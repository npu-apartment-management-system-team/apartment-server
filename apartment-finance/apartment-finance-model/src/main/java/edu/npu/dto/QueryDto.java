package edu.npu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

//UserPayListQueryDto

/**
 * @author wangminan
 * @description 用于获取职工支付列表和分页查询的dto
 * @createDate 2023-07-02 16:45:55
 */
public record QueryDto(
        @NotNull(message = "pageNum不能为空")
        Integer pageNum,

        @NotNull(message = "pageSize不能为空")
        Integer pageSize,

        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        Date beginTime,

        Integer type,

        Integer status,

        Long departmentId
) {
}
