package edu.npu.controller;

import edu.npu.dto.ApartmentDto;
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
        return apartmentService.updateApartment(id, apartmentDto);
    }
}
