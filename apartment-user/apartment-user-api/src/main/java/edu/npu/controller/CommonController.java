package edu.npu.controller;

import edu.npu.dto.UpdatePasswordDto;
import edu.npu.service.CommonService;
import edu.npu.vo.R;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
@Controller
@RequestMapping("/password")
public class CommonController {
    private CommonService commonService;
    @PutMapping
    public R UpdatePassword(@PathVariable("id")Long id, UpdatePasswordDto updatePasswordDto){
        return commonService.updatePassword(id,updatePasswordDto);
    }
}
