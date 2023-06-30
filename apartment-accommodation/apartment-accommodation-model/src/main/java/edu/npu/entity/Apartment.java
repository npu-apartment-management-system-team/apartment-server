package edu.npu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 公寓表
 * @TableName apartment
 */
@TableName(value ="apartment")
@Data
public class Apartment implements Serializable {
    /**
     * apartment唯一ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 负责管理该公寓的主要班组长的在admin表中的id
     */
    private Long foremanAdminId;

    /**
     * 公寓名称，eg.望江门公寓
     */
    private String name;

    /**
     * 公寓所在具体地名,从市一级开始精确到门牌号
     */
    private String position;

    /**
     * 公寓所在地点经度
     */
    private Double positionLongitude;

    /**
     * 公寓所在地点纬度
     */
    private Double positionLatitude;

    /**
     * 公寓状态 0正常 1启用程序中 2弃用程序中 3已弃用
     */
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
