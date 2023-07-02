package edu.npu.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.npu.common.UserPayStatusEnum;
import edu.npu.dto.UserPayListQueryDto;
import edu.npu.entity.AccountUserDetails;
import edu.npu.entity.PaymentUser;
import edu.npu.entity.User;
import edu.npu.exception.ApartmentError;
import edu.npu.exception.ApartmentException;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.mapper.PaymentUserMapper;
import edu.npu.service.PaymentUserService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import static edu.npu.common.AlipayRequestConstants.OUT_TRADE_NUMBER;
import static edu.npu.common.PaymentConstants.PAY_MESSAGE_TTL;

/**
 * @author wangminan
 * @description 针对表【payment_user(自收与代扣自付部分缴费表)】的数据库操作Service实现
 * @createDate 2023-07-02 16:45:55
 */
@Service
@Slf4j
public class PaymentUserServiceImpl extends ServiceImpl<PaymentUserMapper, PaymentUser>
        implements PaymentUserService {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private UserServiceClient userServiceClient;

    @Resource
    private AlipayClient alipayClient;

    @Resource
    private Environment config;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public R getPayList(AccountUserDetails accountUserDetails,
                        UserPayListQueryDto userPayListQueryDto) {
        User user = extractUser(accountUserDetails);
        Page<PaymentUser> page = new Page<>(
                userPayListQueryDto.pageNum(), userPayListQueryDto.pageSize());
        LambdaQueryWrapper<PaymentUser> wrapper = new LambdaQueryWrapper<>();
        if (userPayListQueryDto.beginTime() != null) {
            wrapper.ge(PaymentUser::getCreateTime, userPayListQueryDto.beginTime());
        }
        if (userPayListQueryDto.type() != null) {
            wrapper.eq(PaymentUser::getType, userPayListQueryDto.type());
        }
        if (userPayListQueryDto.status() != null) {
            wrapper.le(PaymentUser::getStatus, userPayListQueryDto.status());
        }
        wrapper.eq(PaymentUser::getUserId, user.getId());
        wrapper.orderByDesc(PaymentUser::getCreateTime);
        page = page(page, wrapper);

        Map<String, Object> result = Map.of(
                "total", page.getTotal(),
                "list", page.getRecords()
        );
        return R.ok(result);
    }

    @Override
    public R startPay(AccountUserDetails accountUserDetails, Long orderId) {
        log.info("收到来自用户:{}对订单:{}的缴费请求,开始处理",
                accountUserDetails.getId(), orderId);

        PaymentUser paymentUser = getById(orderId);
        // 更新当前订单状态
        paymentUser.setStatus(UserPayStatusEnum.PAYING.getValue());
        boolean updatePaymentStatus = updateById(paymentUser);
        if (!updatePaymentStatus) {
            throw new ApartmentException(ApartmentError.UNKNOWN_ERROR,
                    "更新订单状态失败,需要重试支付过程");
        }

        // 调用支付宝接口 可能alt+enter没有候选项，需要自己写import
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();

        request.setNotifyUrl(config.getProperty("alipay.notify-url"));
        request.setReturnUrl(config.getProperty("alipay.return-url"));

        // 以上是配置所有的公共参数，下面是配置请求参数集合
        ObjectNode bizContent = objectMapper.createObjectNode();
        bizContent.put(OUT_TRADE_NUMBER.getType(), orderId.toString());

        bizContent.put("total_amount", paymentUser.getPrice());
        bizContent.put("subject", "西工大拼车平台订单支付");
        // 手机端场景使用QUICK_WAP_WAY
        // 电脑FAST_INSTANT_TRADE_PAY
        // 注意和req res统一
        bizContent.put("product_code", "QUICK_WAP_WAY");
        bizContent.put("quit_url", config.getProperty("alipay.quit-url"));
        // 继续构造请求
        request.setBizContent(bizContent.toString());
        // 调用远程接口
        AlipayTradeWapPayResponse response;
        try {
            response = alipayClient.pageExecute(request);
        } catch (AlipayApiException e) {
            log.error("创建支付交易失败");
            // try-catch与Transaction注解配合使用时需要手动抛出RuntimeException，否则事务不会回滚
            throw new ApartmentException(
                    ApartmentError.UNKNOWN_ERROR,
                    "创建交易失败,未知异常");
        }
        if (response == null) {
            log.error("未收到支付宝回调");
            throw new ApartmentException("创建交易失败,未收到支付宝回调");
        }
        if (response.isSuccess()) {
            log.info("调用支付宝生成支付地址成功" + response.getBody());
        } else {
            log.info("调用支付宝生成支付地址失败," + response.getCode() + " " + response.getMsg());
            throw new ApartmentException("创建支付交易失败,支付宝回调报异常");
        }
        /*
            回跳地址示例
            https://m.alipay.com/Gk8NF23?total_amount=9.00
            &timestamp=2016-08-11+19%3A36%3A01
            &sign=ErCRRVmW%2FvXu1XO76k%2BUr4gYKC5%2FWgZGSo%2FR7nbL%2FPU7yFXtQJ2CjYPcqumxcYYB5x%2FzaRJXWBLN3jJXr01Icph8AZGEmwNuzvfezRoWny6%2Fm0iVQf7hfgn66z2yRfXtRSqtSTQWhjMa5YXE7MBMKFruIclYVTlfWDN30Cw7k%2Fk%3D
            &trade_no=2016081121001004630200142207
            &sign_type=RSA2
            &charset=UTF-8
            &seller_id=2088111111116894
            &method=alipay.trade.wap.pay.return
            &app_id=2016040501024706
            &out_trade_no=70501111111S001111119
            &version=1.0
         */

        // 向rabbitMQ延迟交换机发送消息 设置TTL为30分钟
        // 1.准备消息
        Message message = MessageBuilder
                .withBody(orderId.toString().getBytes(StandardCharsets.UTF_8))
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .setHeader("x-delay", PAY_MESSAGE_TTL)
                .build();
        // 2.准备CorrelationData
        CorrelationData correlationData =
                new CorrelationData(UUID.randomUUID().toString());
        // 3.发送消息
        rabbitTemplate.convertAndSend(
                "delay.direct", "delay", message, correlationData);
        log.info("发送消息成功,uuid:{}", correlationData.getId());

        return R.ok().put("payUrl", response.getBody());
    }

    private User extractUser(AccountUserDetails accountUserDetails) {
        User user = userServiceClient.getUserByLoginAccountId(accountUserDetails.getId());
        if (user == null) {
            throw new ApartmentException(ApartmentError.OBJECT_NULL, "请求的用户不存在");
        }
        return user;
    }
}




