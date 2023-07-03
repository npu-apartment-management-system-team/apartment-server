package edu.npu.feignClient.fallback;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.npu.dto.UserPayListQueryDto;
import edu.npu.entity.Application;
import edu.npu.feignClient.ApplicationServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @author : [wangminan]
 * @description : [远程调用application-api服务失败回调]
 */
@Component
@Slf4j
public class ApplicationServiceClientFallbackFactory
        implements FallbackFactory<ApplicationServiceClient> {


    @Override
    public ApplicationServiceClient create(Throwable cause) {
        return new ApplicationServiceClient() {
            @Override
            public Page<Application> getApplicationPageForQuery(
                    UserPayListQueryDto userPayListQueryDto) {
                log.error("调用application-api服务失败，userPayListQueryDto:{},原因：{}",
                        userPayListQueryDto,
                        cause.getMessage());
                return null;
            }

            @Override
            public List<Application> getApplicationListForDownload(
                    Date beginTime, Long departmentId) {
                log.error("调用application-api服务失败，beginTime:{},departmentId:{},原因：{}",
                        beginTime,
                        departmentId,
                        cause.getMessage());
                return null;
            }
        };
    }
}
