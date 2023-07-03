package edu.npu.feignClient.fallback;

import edu.npu.entity.Department;
import edu.npu.entity.Room;
import edu.npu.feignClient.ManagementServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author : [wangminan]
 * @description : [远程调用management-api服务失败回调]
 */
@Slf4j
@Component
public class ManagementServiceClientFallbackFactory
        implements FallbackFactory<ManagementServiceClient> {
    @Override
    public ManagementServiceClient create(Throwable cause) {
        return new ManagementServiceClient() {
            @Override
            public Department getDepartmentById(Long id) {
                log.error("调用management-api服务获取Department失败，原因：{}", cause.getMessage());
                return null;
            }

            @Override
            public List<Department> getListByShardIndex(Long shardIndex, Integer shardTotal) {
                log.error("调用management-api服务获取Department列表失败，原因：{}", cause.getMessage());
                return null;
            }

            @Override
            public Room getRoomByBedId(Long bedId) {
                log.error("调用management-api服务获取Room失败，原因：{}", cause.getMessage());
                return null;
            }
        };
    }
}
