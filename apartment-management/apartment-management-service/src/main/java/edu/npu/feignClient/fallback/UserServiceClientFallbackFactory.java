package edu.npu.feignClient.fallback;

import edu.npu.entity.Admin;
import edu.npu.entity.User;
import edu.npu.feignClient.UserServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @author : [wangminan]
 * @description : [远程调用user-api服务失败回调]
 */
@Slf4j
@Component
public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient> {
    @Override
    public UserServiceClient create(Throwable cause) {
        return new UserServiceClient() {
            @Override
            public List<Admin> getAdminByDepartmentId(Long departmentId) {
                log.error("调用user-api服务获取Admin列表失败，原因：{}", cause.getMessage());
                return Collections.emptyList();
            }

            @Override
            public User getUserById(Long id) {
                log.error("调用user-api服务获取User失败，原因：{}", cause.getMessage());
                return null;
            }

            @Override
            public User getUserByLoginAccountId(Long loginAccountId) {
                log.error("调用user-api服务获取User失败，原因：{}", cause.getMessage());
                return null;
            }
        };
    }
}
