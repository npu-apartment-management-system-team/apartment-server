package edu.npu.service.impl;

import edu.npu.common.UserPayStatusEnum;
import edu.npu.common.UserPayTypeEnum;
import edu.npu.entity.*;
import edu.npu.feignClient.ManagementServiceClient;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.mapper.PaymentDepartmentMapper;
import edu.npu.mapper.PaymentUserMapper;
import edu.npu.service.GeneratePaymentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author : [wangminan]
 * @description : [生成缴费单]
 */
@Service
@Slf4j
public class GeneratePaymentServiceImpl implements GeneratePaymentService {

    @Resource
    private PaymentDepartmentMapper paymentDepartmentMapper;

    @Resource
    private PaymentUserMapper paymentUserMapper;

    @Resource
    private UserServiceClient userServiceClient;

    @Resource
    private ManagementServiceClient managementServiceClient;

    // 代扣0 自收1
    private static final Integer USER_PAY_WITHHOLD = 0;

    // 网费 都是50块
    private static final Integer NETWORK_FEE = 50;

    @Override
    public void generateDepartmentPayment(Long shardIndex, int shardTotal) {
        List<Department> departmentList =
                managementServiceClient.getListByShardIndex(shardIndex, shardTotal);
        Integer totalPrice = 0;
        for (Department department : departmentList) {
            // 根据department找到住户
            List<User> userList = userServiceClient.getUserListByDepartmentId(
                    department.getId()
            );
            // 根据住户找到房间
            for (User user : userList) {
                Long bedId = user.getBedId();
                if (bedId == null) {
                    continue;
                }
                Room room = managementServiceClient.getRoomByBedId(bedId);
                totalPrice += room.getRefundFee();
            }
            PaymentDepartment paymentDepartment =
                    PaymentDepartment.builder()
                            .departmentId(department.getId())
                            .price(totalPrice)
                            .build();
            int insert = paymentDepartmentMapper.insert(paymentDepartment);
            if (insert != 1) {
                log.error("插入部门缴费单失败,部门id={}", department.getId());
            }
        }
    }

    @Override
    public void generateUserPayment(Long shardIndex, int shardTotal) {
        List<User> userList = userServiceClient.getListByShardIndex(shardIndex, shardTotal);
        if (userList.isEmpty()) {
            return;
        }
        for (User user : userList) {
            if (user.getNetworkEnabled() == 1) {
                // 生成网费订单
                PaymentUser paymentUser =
                    PaymentUser.builder()
                            .userId(user.getId())
                            .price(NETWORK_FEE)
                            .type(UserPayTypeEnum.NETWORK.getValue())
                            .createTime(new Date(System.currentTimeMillis()))
                            .updateTime(new Date(System.currentTimeMillis()))
                            .status(UserPayStatusEnum.UNPAID.getValue())
                            .build();
                int insert = paymentUserMapper.insert(paymentUser);
                if (insert != 1) {
                    log.error("插入网费缴费单失败,用户id={}", user.getId());
                }
            }

            Room room = managementServiceClient.getRoomByBedId(user.getBedId());
            // 查询用户类别是自收还是代扣
            if (user.getPayType().equals(USER_PAY_WITHHOLD)) {
                // 仅支付自扣部分即可
                int price = room.getSelfPayFee();
                PaymentUser paymentUser =
                        PaymentUser.builder()
                                .userId(user.getId())
                                .price(price)
                                .type(UserPayTypeEnum.ACCOMMODATION.getValue())
                                .createTime(new Date(System.currentTimeMillis()))
                                .updateTime(new Date(System.currentTimeMillis()))
                                .status(UserPayStatusEnum.UNPAID.getValue())
                                .build();
                int insert = paymentUserMapper.insert(paymentUser);
                if (insert != 1) {
                    log.error("插入住宿费代扣用户部分缴费单失败,用户id={}", user.getId());
                }
            } else {
                // 自收用户 全都要交
                int price = room.getTotalFee();
                PaymentUser paymentUser =
                        PaymentUser.builder()
                                .userId(user.getId())
                                .price(price)
                                .type(UserPayTypeEnum.ACCOMMODATION.getValue())
                                .createTime(new Date(System.currentTimeMillis()))
                                .updateTime(new Date(System.currentTimeMillis()))
                                .status(UserPayStatusEnum.UNPAID.getValue())
                                .build();
                int insert = paymentUserMapper.insert(paymentUser);
                if (insert != 1) {
                    log.error("插入住宿费自收用户部分缴费单失败,用户id={}", user.getId());
                }
            }

        }
    }
}
