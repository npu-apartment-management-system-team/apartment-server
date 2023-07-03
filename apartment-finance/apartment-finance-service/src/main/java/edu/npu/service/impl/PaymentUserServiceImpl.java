package edu.npu.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConstants;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.npu.common.AlipayTradeState;
import edu.npu.common.RedisConstants;
import edu.npu.common.UserPayStatusEnum;
import edu.npu.common.UserPayTypeEnum;
import edu.npu.dto.UserPayListQueryDto;
import edu.npu.entity.AccountUserDetails;
import edu.npu.entity.PaymentUser;
import edu.npu.entity.User;
import edu.npu.exception.ApartmentError;
import edu.npu.exception.ApartmentException;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.mapper.PaymentUserMapper;
import edu.npu.service.PaymentUserService;
import edu.npu.util.RedisClient;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    @Resource
    private RedisClient redisClient;

    private static final ExecutorService cachedThreadPool =
            Executors.newFixedThreadPool(
                    // 获取系统核数
                    Runtime.getRuntime().availableProcessors()
            );

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

    /**
     * 通过用户id和订单id开始交易
     * @param accountUserDetails 用户信息
     * @param orderId 订单id
     * @return R
     */
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

        paymentUser.setStatus(UserPayStatusEnum.PAYING.getValue());
        updateById(paymentUser);

        return R.ok().put("payUrl", response.getBody());
    }

    /**
     * 支付宝回调验签并确认订单
     * @param notifyParams 支付宝回调参数
     * @return R
     */
    @Override
    public String checkSignAndConfirm(Map<String, String> notifyParams) {
        /*
         * 回调示例如下:
         * 通知参数 ====>
         * {
         * gmt_create=2022-12-05 17:18:37,
         * charset=UTF-8,
         * gmt_payment=2022-12-05 17:18:46,
         * notify_time=2022-12-05 17:18:49,
         * subject=Java课程,
         * sign=FmKXT9HjUhWlbCImoG4zrTUD3fFGYAmAo+2bPBNJi2WqiGNzRHDJ7FT6EOpZULlWlQhg8dKfkJ5WtnxIMNYyoLx3g8t73MsWlzFRvfvT1lVAbzj4G69gs57bOPrE5wuJq6gDpWJ4wHDgMPUTABbulHzy0IGcsEAwdrCmtzmt5Jjrube2HgDxm84LIDzbilxl1TVoCVsxaLJj1WAZ2jmVxm2VcM0h9+e8bu/NacRZktofA5DpC7SYf03TMjWLRc4ih5EYGz2tAX9nt3i1LVyxmlJpvPuMobXgPMRcuzNAqDxcMo/CUEvIVmaDT4iiIU4qgPZ1sqAdrs67mFQep6VZ8A==,
         * buyer_id=2088722003402347,
         * invoice_amount=0.01,
         * version=1.0,
         * notify_id=2022120500222171848002340521336546,
         * fund_bill_list=[{"amount":"0.01","fundChannel":"ALIPAYACCOUNT"}],
         * notify_type=trade_status_sync,
         * out_trade_no=ORDER_20221205171809023,
         * total_amount=0.01,
         * trade_status=TRADE_SUCCESS,
         * trade_no=2022120522001402340501695232,
         * auth_app_id=2021000121691518,
         * receipt_amount=0.01,
         * point_amount=0.00,
         * app_id=2021000121691518,
         * buyer_pay_amount=0.01,
         * sign_type=RSA2,
         * seller_id=2088621993877332
         * }
         */
        log.info("收到支付宝通知回调,通知参数 ====> {}", notifyParams);
        String result = "failure";
        // 1.验签 如果成功返回success，否则返回failure(支付宝要求)
        boolean signVerified = false; //调用SDK验证签名
        try {
            signVerified = AlipaySignature.rsaCheckV1(
                    notifyParams,
                    config.getProperty("alipay.alipay-public-key"),
                    AlipayConstants.CHARSET_UTF8,
                    AlipayConstants.SIGN_TYPE_RSA2);
        } catch (AlipayApiException e) {
            log.error("验签参数校验失败:{}", e.getMessage());
            throw new ApartmentException(ApartmentError.PARAMS_ERROR, "支付宝验签参数校验失败");
        }
        if (signVerified) {
            /*
                验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，
                 校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            */
            /*
             * 程序执行完后必须打印输出“success”（不包含引号）。
             * 如果商家反馈给支付宝的字符不是 success 这7个字符，支付宝服务器会不断重发通知，直到超过 24 小时 22 分钟。
             * 一般情况下，25 小时以内完成 8 次通知()
             */
            log.info("支付宝异步通知验签成功");
            /*
             * 进一步校验以下4项业务参数
             * 1.商家需要验证该通知数据中的 out_trade_no 是否为商家系统中创建的订单号。
             * 2.判断 total_amount 是否确实为该订单的实际金额（即商家订单创建时的金额）。
             * 3.校验通知中的 seller_id（或者 seller_email) 是否为 out_trade_no 这笔单据的对应的操作方
             * （有的时候，一个商家可能有多个 seller_id/seller_email）。
             * 4.验证 app_id 是否为该商家本身。
             */

            // out_trade_no
            String outTradeNo = notifyParams.get(OUT_TRADE_NUMBER.getType());
            // 获取订单对象
            PaymentUser paymentUser = getById(Long.parseLong(outTradeNo));


            if (paymentUser == null) {
                log.error("支付宝给出的订单不存在，订单号:{}", outTradeNo);
                return result;
            }

            // total_amount
            String totalAmount = notifyParams.get("total_amount");
            // total_amount中的金额以元为单位，需要转换为分(数据库中记录的单位为分)
            int totalAmountInt = new BigDecimal(totalAmount).intValue();

            int originalPrice = paymentUser.getPrice();
            if (originalPrice != totalAmountInt){
                log.error("订单金额不一致，订单号:{}，订单金额:{}，支付宝金额:{}",
                        outTradeNo, originalPrice, totalAmountInt);
                return result;
            }

            // seller_id
            String sellerId = notifyParams.get("seller_id");
            if (!sellerId.equals(config.getProperty("alipay.seller-id"))) {
                log.error("商家pid不一致，订单号:{}，商家id:{}，支付宝商家id:{}",
                        outTradeNo, sellerId, config.getProperty("alipay.seller-id"));
                return result;
            }

            // app_id
            String appId = notifyParams.get("app_id");
            if (!appId.equals(config.getProperty("alipay.app-id"))) {
                log.error("app-id不一致，订单号:{}，商家app-id:{}，支付宝app-id:{}",
                        outTradeNo, appId, config.getProperty("alipay.app-id"));
                return result;
            }

            // 只有交易通知状态为 TRADE_SUCCESS 或 TRADE_FINISHED 时，支付宝才会认定为买家付款成功。
            // 前者支持退款 后者不支持退款
            String tradeStatus = notifyParams.get("trade_status");
            if (!tradeStatus.equals(AlipayTradeState.SUCCESS.getType())) {
                log.error("支付未完成，订单号:{}，交易状态:{}", outTradeNo, tradeStatus);
                paymentUser.setStatus(UserPayStatusEnum.UNPAID.getValue());
                boolean success = updateById(paymentUser);
                if (success) {
                    log.error("支付未完成，更新订单状态失败，订单号:{}", outTradeNo);
                }
                return result;
            }

            // 加锁,并发控制,防止重复回调
            /*
             * 注意:锁与@Transaction注解不兼容，如果使用了@Transaction注解，那么锁将失效
             * 1.如果使用了@Transaction注解，那么在方法执行完后，会自动提交事务，此时锁将自动释放
             * 2.如果没有使用@Transaction注解，那么在方法执行完后，需要手动提交事务，此时锁才会释放
             * 参考 https://blog.csdn.net/fal1230/article/details/113392123
             */
            if (redisClient.tryLock(
                RedisConstants.LOCK_PAYMENT_USER_KEY + paymentUser.getId())) {
                try {
                    // 处理重复通知
                    // 无论接口被调用多少次 以下业务只执行一次
                    // 接口调用的幂等性

                    // 处理自身业务
                    log.info("处理订单");
                    // 修改订单状态 订单正常结束
                    paymentUser.setStatus(UserPayStatusEnum.PAID.getValue());
                    // 记录支付日志
                    log.info("订单号:{} 回调已收到，参数正常，状态已更新", outTradeNo);
                    updateById(paymentUser);
                } finally {
                    redisClient.unlock(
                    RedisConstants.LOCK_PAYMENT_USER_KEY + paymentUser.getId()
                    );
                }
            }
            result = "success";
        } else {
            log.error("支付宝异步通知验签失败");
        }
        return result;
    }

    /**
     * 支付宝退款接口 用来退押金的
     * @param userId 用户id
     * @return R
     */
    @Override
    public boolean refundDepositCharge(Long userId) {
        // 退款
        User user = userServiceClient.getUserById(userId);
        if (user == null) {
            throw new ApartmentException(ApartmentError.OBJECT_NULL, "请求的用户不存在");
        }
        // 查表
        List<PaymentUser> paymentUsers =
                list(new LambdaQueryWrapper<PaymentUser>()
                        .eq(PaymentUser::getUserId, userId)
                        .eq(PaymentUser::getType, UserPayTypeEnum.DEPOSIT.getValue())
                        .orderByDesc(PaymentUser::getCreateTime)
                );
        // 最新的一次记录
        PaymentUser paymentUser = paymentUsers.get(0);

        log.info("收到来自用户:{}对订单:{}的押金退款请求", userId, paymentUser.getId());
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        ObjectNode bizContent = objectMapper.createObjectNode();
        bizContent.put("out_trade_no", paymentUser.getId());
        bizContent.put("refund_amount", paymentUser.getPrice());

        request.setBizContent(bizContent.toString());
        AlipayTradeRefundResponse response;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            log.error(e.getMessage());
            throw new ApartmentException(
                    ApartmentError.UNKNOWN_ERROR,
                    "服务器异常,支付宝对订单退款失败:" + paymentUser.getId());
        }
        if(response.isSuccess()){
            log.info("调用支付宝退款接口成功,用户:{}的押金已退回",user);
            return true;
        }
        log.error("退款失败,用户:{},订单号:{}", user, paymentUser.getId());
        return false;
    }

    /**
     * 支付宝订单查询接口
     * @param orderId 订单号
     * @return R
     */
    @Override
    public boolean tradeQuery(String orderId) {
        PaymentUser paymentUser = getById(orderId);
        if (paymentUser.getStatus() == UserPayStatusEnum.PAID.getValue()) {
            log.info("订单号:{}已完成支付,无需查单",orderId);
            return true;
        }
        // 开始查单
        log.info("开始查询订单号:{}的支付状态",orderId);
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        ObjectNode bizContent = objectMapper.createObjectNode();
        bizContent.put("out_trade_no", orderId);
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "服务器异常,支付宝查单失败");
        }
        if(response.isSuccess()){
            // 查看具体状态
            if (response.getTradeStatus().equals(AlipayTradeState.SUCCESS.getType())) {
                // 更新订单状态到已完成
                paymentUser.setStatus(UserPayStatusEnum.PAID.getValue());
            } else if (response.getTradeStatus().equals(AlipayTradeState.CLOSED.getType())) {
                // 两种情况 超时关单/退款
                // 查询订单是否退款
                if (!refundQuery(orderId)) {
                    log.error("订单:{}支付已超时,正在开启新订单",orderId);
                    PaymentUser newPaymentUser = PaymentUser.builder().build();
                    BeanUtils.copyProperties(paymentUser, newPaymentUser);
                    newPaymentUser.setId(null);
                    newPaymentUser.setStatus(UserPayStatusEnum.UNPAID.getValue());
                    newPaymentUser.setCreateTime(new Date(System.currentTimeMillis()));
                    newPaymentUser.setUpdateTime(new Date(System.currentTimeMillis()));
                    save(newPaymentUser);
                } else {
                    log.info("订单:{}已退款",orderId);
                }
            } else if (response.getTradeStatus().equals(
                    AlipayTradeState.NOT_PAY.getType())) {
                log.error("订单:{}支付未完成", orderId);
                paymentUser.setStatus(UserPayStatusEnum.UNPAID.getValue());
                updateById(paymentUser);
            }
        } else {
            log.error("订单不存在:{}",orderId);
            return false;
        }
        return true;
    }

    private boolean refundQuery(String orderId) {
        AlipayTradeFastpayRefundQueryRequest request =
                new AlipayTradeFastpayRefundQueryRequest();
        ObjectNode bizContent = objectMapper.createObjectNode();
        bizContent.put("out_request_no", orderId);

        request.setBizContent(bizContent.toString());
        AlipayTradeFastpayRefundQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            throw new ApartmentException(ApartmentError.UNKNOWN_ERROR, "服务器异常,支付宝查单失败");
        }
        if(response.isSuccess()){
            return response.getRefundStatus().equals(
                    AlipayTradeState.REFUND_SUCCESS.getType()
            );
        } else {
            log.error("订单不存在:{}",orderId);
            return false;
        }
    }

    private User extractUser(AccountUserDetails accountUserDetails) {
        User user = userServiceClient.getUserByLoginAccountId(accountUserDetails.getId());
        if (user == null) {
            throw new ApartmentException(ApartmentError.OBJECT_NULL, "请求的用户不存在");
        }
        return user;
    }
}




