package edu.npu.feignClient.fallback;

import edu.npu.feignClient.FinanceServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @author : [wangminan]
 * @description : [远程调用finance-api服务失败回调]
 */
@Slf4j
@Component
public class FinanceServiceClientFallbackFactory
        implements FallbackFactory<FinanceServiceClient> {
    @Override
    public FinanceServiceClient create(Throwable cause) {
        return new FinanceServiceClient() {
            @Override
            public boolean addDepositCharge(Long userId) {
                log.error("调用远程新增收费信息接口失败,用户:{},原因:{}",
                        userId, cause.getMessage());
                return false;
            }

            @Override
            public boolean refundDepositCharge(Long userId) {
                log.error("调用远程退款接口失败,用户:{},原因:{}",
                        userId, cause.getMessage());
                return false;
            }
        };
    }
}
