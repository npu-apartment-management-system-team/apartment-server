package edu.npu.dto;

public record UserListQueryDto(
        Integer pageNum,
        Integer pageSize,
        String query,
        Long departmentId,
        Long apartmentId
) {
}
