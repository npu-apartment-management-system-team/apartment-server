package edu.npu.vo;

import edu.npu.doc.ApartmentDoc;

import java.util.List;

public record ApartmentPageResultVo(
        Long total,
        List<ApartmentDoc> data
) {
}
