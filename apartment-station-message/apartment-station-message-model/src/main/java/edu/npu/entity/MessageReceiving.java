package edu.npu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 消息接收表。
 * @TableName message_receiving
 */
@TableName(value ="message_receiving")
@Data
public class MessageReceiving implements Serializable {
    /**
     * 消息接收记录的唯一ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 逻辑外键。message_detail表中消息的ID。
     */
    @JsonAlias(value = "message_detail_id")
    private Long messageDetailId;

    /**
     * 逻辑外键。消息接收者在admin表中的ID。 可null
     */
    @JsonAlias(value = "receiver_admin_id")
    private Long receiverAdminId;

    /**
     * 逻辑外键。消息接收者在user表中的ID。 与admin_id不可同时为空
     */
    @JsonAlias(value = "receiver_user_id")
    private Long receiverUserId;

    /**
     * 是否确认收到。0未收到 1已收到 default 0
     */
    @JsonAlias(value = "is_acked")
    private Integer isAcked;

    /**
     * 是否已删除 处的is_deleted不是传统的逻辑删除字段 0未删除 1已删除 default 0
     */
    @JsonAlias(value = "is_deleted")
    private Integer isDeleted;

    @TableField(exist = false)
    @Serial
    private static final long serialVersionUID = 1L;
}
