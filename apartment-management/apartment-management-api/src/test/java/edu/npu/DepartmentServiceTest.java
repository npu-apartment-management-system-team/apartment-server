package edu.npu;

import edu.npu.dto.DepartmentDto;
import edu.npu.dto.DepartmentPageQueryDto;
import edu.npu.service.DepartmentService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author: Yu
 * @Date: 2023/6/29
 */
@SpringBootTest
class DepartmentServiceTest {

    @Resource
    private DepartmentService departmentService;

    @Test
    void addTest() {
        DepartmentDto departmentDto = new DepartmentDto("西北工业大学长安校区", 1, 0, "西安市", 108.765108, 34.033330);
        departmentService.addDepartment(departmentDto);
    }

    @Test
    void deleteTest() {
        System.out.printf(departmentService.deleteDepartment(1L).toString());;

    }

    @Test
    void updateTest() {
        DepartmentDto departmentDto = new DepartmentDto("西北工业大学友谊校区", 1, 0, "西安市", 108.765108, 34.033330);
        departmentService.updateDepartment(1L, departmentDto);

    }

    @Test
    void getDepartmentListTest() {
        DepartmentPageQueryDto departmentPageQueryDto = new DepartmentPageQueryDto(1, 2, null, 108.765108, 34.03333);
        DepartmentPageQueryDto departmentPageQueryDto2 = new DepartmentPageQueryDto(1, 2, "西北", null, null);
        departmentService.getDepartmentList(departmentPageQueryDto);
        departmentService.getDepartmentList(departmentPageQueryDto2);
    }

    @Test
    void getDepartmentSimpleListTest() {
        System.out.printf(departmentService.getDepartmentSimpleList().toString());
    }

    @Test
    void getDepartmentDetailTest() {
        System.out.printf(departmentService.getDepartmentDetail(1L).toString());
    }

}
