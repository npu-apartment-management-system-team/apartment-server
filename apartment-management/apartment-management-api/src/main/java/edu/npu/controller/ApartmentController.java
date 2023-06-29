package edu.npu.controller;

import edu.npu.service.ApartmentService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : [wangminan]
 * @description : [公寓控制器类]
 */
@RestController
@RequestMapping("/mapping")
public class ApartmentController {

    @Resource
    private ApartmentService apartmentService;

    @PostMapping
    public R addApartment() {
        return R.ok();
    }
}
