package edu.npu.doc;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author : [wangminan]
 * @description : [消息在ElasticSearch中的Mapping映射]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDoc {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String message;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long senderAdminId;

    private String senderAdminName;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Integer isDeleted;

    // 一个消息的管理者数组 包括发送者和接收者 需要对应ES的数组类型
    private Long[] receiverIds;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date createTime;
}
