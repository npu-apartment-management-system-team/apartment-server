package edu.npu.feignClient;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.npu.dto.QueryDto;
import edu.npu.entity.Application;
import edu.npu.feignClient.fallback.ManagementServiceClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;

/**
 * @author : [wangminan]
 * @description : [Application服务的远程调用接口]
 */
@FeignClient(value = "application-api",
        path = "/application/remote",
        fallbackFactory = ManagementServiceClientFallbackFactory.class)
public interface ApplicationServiceClient {

    @GetMapping("/query/page")
    Page<Application> getApplicationPageForQuery(
            @Validated QueryDto queryDto,
            @RequestParam(value = "departmentId") Long departmentId
    );

    @GetMapping("/download/list")
    List<Application> getApplicationListForDownload(
            @RequestParam(value = "beginTime", required = false) Date beginTime,
            @RequestParam(value = "departmentId", required = false) Long departmentId
    );
}
