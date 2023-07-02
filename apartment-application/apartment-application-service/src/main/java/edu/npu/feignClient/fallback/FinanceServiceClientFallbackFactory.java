package edu.npu.feignClient.fallback;

import edu.npu.feignClient.FinanceServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;
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
        return userId -> {
            log.error("调用生成押金订单接口失败,用户:{},error:{}", userId, cause);
            return false;
        };
    }
}
