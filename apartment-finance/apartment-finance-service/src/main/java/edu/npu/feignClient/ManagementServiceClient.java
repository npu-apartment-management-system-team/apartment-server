package edu.npu.feignClient;

import edu.npu.entity.Department;
import edu.npu.entity.Room;
import edu.npu.feignClient.fallback.ManagementServiceClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "management-api",
        path = "/management/remote",
        fallbackFactory = ManagementServiceClientFallbackFactory.class)
public interface ManagementServiceClient {

    @GetMapping("/department")
    Department getDepartmentById(@RequestParam(value = "id") Long id);

    @GetMapping("/department/list/shard")
    List<Department> getListByShardIndex(
            @RequestParam(value = "shardIndex") Long shardIndex,
            @RequestParam(value = "shardTotal") Integer shardTotal);

    @GetMapping("/room/bedId")
    Room getRoomByBedId(@RequestParam(value = "bedId") Long bedId);
}
