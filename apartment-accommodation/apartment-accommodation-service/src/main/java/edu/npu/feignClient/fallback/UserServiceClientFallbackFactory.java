package edu.npu.feignClient.fallback;

import edu.npu.entity.User;
import edu.npu.feignClient.UserServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * @author : [wangminan]
 * @description : [用户远程调用服务降级]
 */
@Slf4j
public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient> {
    @Override
    public UserServiceClient create(Throwable cause) {
        return new UserServiceClient() {
            @Override
            public User getUserByLoginAccountId(Long loginAccountId) {
                log.error("远程调用user-api获取用户服务失败,原因:{}", cause.getMessage());
                return null;
            }
        };
    }
}
