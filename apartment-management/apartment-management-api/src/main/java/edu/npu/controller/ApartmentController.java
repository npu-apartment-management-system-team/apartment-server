package edu.npu.controller;

import edu.npu.common.ResponseCodeEnum;
import edu.npu.dto.ApartmentCenterPageQueryDto;
import edu.npu.dto.ApartmentDto;
import edu.npu.dto.ApartmentPageQueryDto;
import edu.npu.service.ApartmentService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author : [wangminan]
 * @description : [公寓控制器类]
 */
@RestController
@RequestMapping("/apartment")
public class ApartmentController {

    @Resource
    private ApartmentService apartmentService;

    @PostMapping
    public R addApartment(@Validated @RequestBody ApartmentDto apartmentDto) {
        return apartmentService.addApartment(apartmentDto);
    }

    @DeleteMapping("/{id}")
    public R deleteApartment(@PathVariable Long id) {
        return apartmentService.deleteApartment(id);
    }

    @PutMapping("/{id}")
    public R updateApartment(@PathVariable Long id,
                             @Validated @RequestBody ApartmentDto apartmentDto) {
        if (apartmentDto.positionLatitude() > 90 ||
                apartmentDto.positionLatitude() < -90 ||
                apartmentDto.positionLongitude() > 180 ||
                apartmentDto.positionLongitude() < -180
        ) {
            return R.error(
                    ResponseCodeEnum.PRE_CHECK_FAILED,
                    "经纬度范围不正确");
        }
        return apartmentService.updateApartment(id, apartmentDto);
    }

    /**
     * 获取公寓完整信息表格
     * @param apartmentPageQueryDto 查询条件
     * @return 公寓完整信息表格
     */
    @GetMapping
    public R getApartmentList(@Validated ApartmentPageQueryDto apartmentPageQueryDto) {
        if (apartmentPageQueryDto.latitude() != null &&
                apartmentPageQueryDto.longitude() != null) {
            if (apartmentPageQueryDto.latitude() > 90 ||
                    apartmentPageQueryDto.latitude() < -90 ||
                    apartmentPageQueryDto.longitude() > 180 ||
                    apartmentPageQueryDto.longitude() < -180) {
                return R.error(
                        ResponseCodeEnum.PRE_CHECK_FAILED,
                        "经纬度范围不正确");
            }
        }
        return apartmentService.getApartmentList(apartmentPageQueryDto);
    }

    /**
     * 获取公寓简表
     * @return R
     */
    @GetMapping("/list")
    public R getApartmentSimpleList() {
        return apartmentService.getApartmentSimpleList();
    }

    /**
     * 获取某一公寓的具体信息 走Redis
     * @param id 公寓id
     * @return R
     */
    @GetMapping("/detail")
    public R getApartmentDetail(@RequestParam Long id) {
        return apartmentService.getApartmentDetail(id);
    }

    @GetMapping("/center/list")
    public R getApartmentListForAllocationClerk(
            @Validated ApartmentCenterPageQueryDto apartmentCenterPageQueryDto) {
        return apartmentService.getApartmentListForAllocationClerk(
                apartmentCenterPageQueryDto);
    }
}
