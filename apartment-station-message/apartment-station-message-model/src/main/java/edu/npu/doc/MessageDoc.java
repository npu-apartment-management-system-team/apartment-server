package edu.npu.doc;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * @author : [wangminan]
 * @description : [消息在ElasticSearch中的Mapping映射]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDoc {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String message;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long senderAdminId;

    private String senderAdminName;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Integer isWithdraw;

    // 一个消息的管理者数组 包括发送者和接收者 需要对应ES的数组类型
    private List<Long> receiverIds;

    private List<Long> adminIds;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date createTime;
}
