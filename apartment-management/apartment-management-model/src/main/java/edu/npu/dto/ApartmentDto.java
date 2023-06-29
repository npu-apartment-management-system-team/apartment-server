package edu.npu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long foremanAdminId,

        /*
         * 公寓名称，eg.望江门公寓
         */
        @NotEmpty
        String name,

        /*
         * 公寓所在具体地名,从市一级开始精确到门牌号
         */
        @NotEmpty
        String position,

        /*
         * 公寓所在地点经度
         */
        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Double positionLongitude,

        /*
         * 公寓所在地点纬度
         */
        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Double positionLatitude,

        /*
         * 公寓状态 0正常 1启用程序中 2弃用程序中 3已弃用
         */
        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Integer status

) {

}
