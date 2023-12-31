package edu.npu.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 部门表
 * @TableName department
 */
@Data
public class Department implements Serializable {
    /**
     * department唯一ID
     */
    private Long id;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 部门内外网类型。0外网 1内网 default1
     */
    private Integer isInterior;

    /**
     * 部门缴费类型。 0按月 1按季度 default 0
     */
    private Integer payType;

    /**
     * 单位所在具体地名，从市一级开始精确到门牌号
     */
    private String position;

    /**
     * 单位所在地点经度
     */
    private Double positionLongitude;

    /**
     * 单位所在地点纬度
     */
    private Double positionLatitude;

    /**
     * 是否已删除 0未删除 1已删除 default 0
     */
    private Integer isDeleted;

    @Serial
    private static final long serialVersionUID = 1L;
}
