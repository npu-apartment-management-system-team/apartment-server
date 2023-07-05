package edu.npu.controller;

import edu.npu.dto.AdminDto;
import edu.npu.dto.AdminPageQueryDto;
import edu.npu.service.AdminService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author: Yu
 * @Date: 2023.6.28
 */

@SpringBootTest
class AdminServiceTest {

    @Resource
    private AdminService adminService;

    @Test
    void addTest() {
        AdminDto adminDto = new AdminDto("18888888888", "e0FqeTS/s+/kBrzF6jZg3yKQ/F25qnm+OPE4kAeqA0crzg119TqfX5NN0W6Givc2eLmO+b80E5VL/Ps+GSMlXCmRKUkGcNVZT9YZUA3vdzLuoHBJW7k+87VVs0uY3/Ye5d89blVW22bnPkCXwhzFqYSrHBbTv8GjYU0ybqmnv7kqRsxQTUbfqaiOZzc6WPAdLN//0IezSGEOs/tN6AovsZpO9p5ymC8YgQLacMD+23u+om1s47pdIMyKM5FOMwzV4OV80joVtCFtq0gawIm/4W9o7UikrCnN7XvJ+IdgBhF15k7Skeee4/RC7ehscLADskpXzb3sC9hcGxxeztvOpQ==", 0, "俞淑敏", 1L, "850778435@qq.com");
        adminService.addAdmin(adminDto);
    }

    @Test
    void deleteTest() {
        System.out.printf(adminService.deleteAdmin(3L).toString());;

    }

    @Test
    void updateTest() {
        AdminDto adminDto = new AdminDto("18368847619", "e0FqeTS/s+/kBrzF6jZg3yKQ/F25qnm+OPE4kAeqA0crzg119TqfX5NN0W6Givc2eLmO+b80E5VL/Ps+GSMlXCmRKUkGcNVZT9YZUA3vdzLuoHBJW7k+87VVs0uY3/Ye5d89blVW22bnPkCXwhzFqYSrHBbTv8GjYU0ybqmnv7kqRsxQTUbfqaiOZzc6WPAdLN//0IezSGEOs/tN6AovsZpO9p5ymC8YgQLacMD+23u+om1s47pdIMyKM5FOMwzV4OV80joVtCFtq0gawIm/4W9o7UikrCnN7XvJ+IdgBhF15k7Skeee4/RC7ehscLADskpXzb3sC9hcGxxeztvOpQ==", 1, "Yu", 1L, "850778435@qq.com");
        adminService.updateAdmin(3L, adminDto);
    }

    @Test
    void getAdminListTest() {
        AdminPageQueryDto adminPageQueryDto1 = new AdminPageQueryDto(1, 2, null, null);
        AdminPageQueryDto adminPageQueryDto2 = new AdminPageQueryDto(2, 2, null, null);
        AdminPageQueryDto adminPageQueryDto3 = new AdminPageQueryDto(1, 2, "L", null);
        AdminPageQueryDto adminPageQueryDto4 = new AdminPageQueryDto(1, 2, null, 1L);
        AdminPageQueryDto adminPageQueryDto5 = new AdminPageQueryDto(1, 2, "俞淑敏", 1L);
        System.out.printf(adminService.getAdminList(adminPageQueryDto1).toString());
        System.out.printf(adminService.getAdminList(adminPageQueryDto2).toString());
        System.out.printf(adminService.getAdminList(adminPageQueryDto3).toString());
        System.out.printf(adminService.getAdminList(adminPageQueryDto4).toString());
        System.out.printf(adminService.getAdminList(adminPageQueryDto5).toString());

    }

    @Test
    void getForemanListTest() {
        System.out.printf(adminService.getForemanList().toString());
    }

}
