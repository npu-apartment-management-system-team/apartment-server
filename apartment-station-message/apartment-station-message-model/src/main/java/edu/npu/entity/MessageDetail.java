package edu.npu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 消息详情表
 * @TableName message_detail
 */
@TableName(value ="message_detail")
@Data
public class MessageDetail implements Serializable {
    /**
     * 消息的唯一ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 消息内容。应该可以做markdown
     */
    private String message;

    /**
     * 逻辑外键。发信者在admin表中对应的id字段的值。
     */
    @JsonAlias("sender_admin_id")
    private Long senderAdminId;

    /**
     * 消息发送的时间
     */
    @JsonAlias("create_time")
    private Date createTime;

    /**
     * 是否被撤回 0正常 1被发送者撤回 default 0
     */
    @JsonAlias("is_withdrawn")
    private Integer isWithdrawn;

    /**
     * 是否已删除 0未删除 1已删除 default 0 不是传统逻辑删除字段
     */
    @JsonAlias("is_deleted")
    private Integer isDeleted;

    @TableField(exist = false)
    @Serial
    private static final long serialVersionUID = 1L;
}
