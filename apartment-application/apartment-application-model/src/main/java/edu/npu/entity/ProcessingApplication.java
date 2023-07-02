package edu.npu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 正在进行中的申请表
 * @TableName processing_application
 */
@TableName(value ="processing_application")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingApplication implements Serializable {
    /**
     * processing_application唯一ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 逻辑外键 与application表的ID构成映射关系
     */
    private Long applicationId;

    @TableField(exist = false)
    @Serial
    private static final long serialVersionUID = 1L;
}
