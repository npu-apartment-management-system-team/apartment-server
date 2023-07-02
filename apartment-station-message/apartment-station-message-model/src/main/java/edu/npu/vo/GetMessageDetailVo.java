package edu.npu.vo;

import edu.npu.entity.MessageDetail;
import edu.npu.entity.MessageReceiving;

import java.util.List;

public record GetMessageDetailVo(
        MessageDetail messageDetail,
        List<MessageReceiving> messageReceivingList
) {
}
