package edu.npu.feignClient;

import edu.npu.entity.Admin;
import edu.npu.entity.User;
import edu.npu.feignClient.fallback.UserServiceClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author : [wangminan]
 * @description : [远程调用user-api服务]
 */
@FeignClient(value = "user-api",
        path = "/user/remote",
        fallbackFactory = UserServiceClientFallbackFactory.class)
public interface UserServiceClient {
    @GetMapping("/user/loginAccountId")
    User getUserByLoginAccountId(
            @RequestParam(value = "loginAccountId") Long loginAccountId);

    @GetMapping("/admin/loginAccountId")
    Admin getAdminByLoginAccountId(
            @RequestParam(value = "id") Long id);

    @GetMapping("/user/department/list")
    List<User> getUserListByDepartmentId(
            @RequestParam(value = "departmentId") Long departmentId);

    @GetMapping("/user")
    User getUserById(@RequestParam(value = "id") Long id);

    @PutMapping("/user")
    boolean updateUser(@RequestBody User user);

    @GetMapping("/user/bed")
    List<User> getUserByBedId(@RequestParam(value = "bedId") Long bedId);
}
