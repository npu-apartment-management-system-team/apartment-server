package edu.npu.vo;

import edu.npu.doc.MessageDoc;

import java.util.List;

public record PageResultVo(
        Long total,
        List<MessageDoc> data
) {
}
