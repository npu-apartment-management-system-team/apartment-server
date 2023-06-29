import edu.npu.dto.AdminPageQueryDto;
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
public class DepartmentServiceTest {

    @Resource
    private DepartmentService departmentService;

    @Test
    public void addTest() {
        DepartmentDto departmentDto = new DepartmentDto("西北工业大学", 1, 0, "西安市", "108.765108", "34.033330");
        departmentService.addDepartment(departmentDto);
    }

    @Test
    public void deleteTest() {
        System.out.printf(departmentService.deleteDepartment(1L).toString());;

    }

    @Test
    public void updateTest() {
        DepartmentDto departmentDto = new DepartmentDto("西北工业大学长安校区", 1, 0, "西安市", "108.765108", "34.033330");
        departmentService.updateDepartment(1L, departmentDto);

    }

    @Test
    public void getDepartmentListTest() {
        DepartmentPageQueryDto departmentPageQueryDto = new DepartmentPageQueryDto(1, 2, null, "108.765108", "34.033330");

    }

    @Test
    public void getDepartmentSimpleListTest() {
        System.out.printf(departmentService.getDepartmentSimpleList().toString());
    }

    @Test
    public void getDepartmentDetailTest() {
        System.out.printf(departmentService.getDepartmentDetail(1L).toString());
    }

}
