package edu.npu.vo;

import edu.npu.doc.UserDoc;

import java.util.List;

public record PageResultVo(
        Long total,
        List<UserDoc> data
) {
}
