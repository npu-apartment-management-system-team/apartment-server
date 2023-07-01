package edu.npu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * @author : [wangminan]
 * @description : [公寓DTO]
 */
public record ApartmentDto(
        /*
         * 负责管理该公寓的主要班组长的在admin表中的id
         */
        @NotNull(message = "班组长id不能为空")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long foremanAdminId,

        /*
         * 公寓名称，eg.望江门公寓
         */
        @NotEmpty(message = "公寓名称不可为空")
        String name,

        /*
         * 公寓所在具体地名,从市一级开始精确到门牌号
         */
        @NotEmpty(message = "位置不可为空")
        String position,

        /*
         * 公寓所在地点经度
         */
        @NotNull(message = "经度不能为空")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @DecimalMax(value = "180", message = "经度最大值为180")
        @DecimalMax(value = "-180", message = "经度最小值为-180")
        Double positionLongitude,

        /*
         * 公寓所在地点纬度
         */
        @NotNull(message = "纬度不能为空")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @DecimalMax(value = "90", message = "纬度最大值为90")
        @DecimalMax(value = "-90", message = "纬度最小值为-90")
        Double positionLatitude,

        /*
         * 公寓状态 0正常 1启用程序中 2弃用程序中 3已弃用
         */
        @NotNull(message = "公寓状态不能为空")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Integer status

) {

}
