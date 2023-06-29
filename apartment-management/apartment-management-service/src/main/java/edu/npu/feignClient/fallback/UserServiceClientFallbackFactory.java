package edu.npu.feignClient.fallback;

import edu.npu.feignClient.UserServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @author : [wangminan]
 * @description : [远程调用user-api服务失败回调]
 */
@Slf4j
@Component
public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient> {
    @Override
    public UserServiceClient create(Throwable cause) {
        return departmentId -> {
            log.error("远程调用user-api服务失败,原因:{}", cause.getMessage());
            return null;
        };
    }
}
