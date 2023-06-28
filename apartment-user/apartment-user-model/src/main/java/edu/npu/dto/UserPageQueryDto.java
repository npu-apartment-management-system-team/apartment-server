package edu.npu.dto;

public record UserPageQueryDto(
        Integer pageNum,
        Integer pageSize,
        String query,
        Long departmentId,
        Long apartmentId
) {
}
