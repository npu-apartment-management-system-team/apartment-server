package edu.npu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * @Author: Yu
 * @Date: 2023/7/3
 */
public record DownloadQueryDto(

        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
        Date beginTime,

        Long departmentId
) {

}
