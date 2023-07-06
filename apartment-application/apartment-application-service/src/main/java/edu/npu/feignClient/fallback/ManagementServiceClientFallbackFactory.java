package edu.npu.feignClient.fallback;

import edu.npu.entity.Apartment;
import edu.npu.entity.Bed;
import edu.npu.entity.Department;
import edu.npu.entity.Room;
import edu.npu.feignClient.ManagementServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

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
            public Room getRoomById(Long id) {
                log.error("调用management-api服务获取Room失败，原因：{}", cause.getMessage());
                return null;
            }

            @Override
            public Bed getBedById(Long id) {
                log.error("调用management-api服务获取Bed失败，原因：{}", cause.getMessage());
                return null;
            }

            @Override
            public boolean updateBed(Bed bed) {
                log.error("调用management-api服务更新Bed失败，原因：{}", cause.getMessage());
                return false;
            }

            @Override
            public Apartment getApartmentById(Long id) {
                log.error("调用management-api服务获取Apartment失败，原因：{}", cause.getMessage());
                return null;
            }
        };
    }
}
