package edu.npu.controller;

import edu.npu.dto.AddBedDto;
import edu.npu.dto.BedPageQueryDto;
import edu.npu.dto.BedQueryDto;
import edu.npu.dto.UpdateBedDto;
import edu.npu.service.BedService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
@RestController
@RequestMapping("/bed")
public class BedController {
    @Resource
    private BedService bedService;
    @GetMapping
    public R getBedList(@Validated BedPageQueryDto bedPageQueryDto){
        return bedService.getBedList(bedPageQueryDto);
    }

    @GetMapping("/detail")
    public R getBedById(BedQueryDto bedQueryDto){
        return bedService.getBedById(bedQueryDto);
    }

    @PostMapping
    public R addBed(AddBedDto addBedDto){
        return bedService.addBed(addBedDto);
    }

    @PutMapping("{id}")
    public R updateBed(@PathVariable("id") Long id, UpdateBedDto updateBedDto){
        return bedService.updateBed(id, updateBedDto);
    }

    @DeleteMapping("{id}")
    public R deleteBed(@PathVariable("id") Long id){
        return bedService.deleteBed(id);
    }
}
