package edu.npu.dto;

import jakarta.validation.constraints.NotNull;

public record AddRoomDto(
        @NotNull
        Long apartmentId,
        @NotNull
        String name,
        @NotNull
        String usage,
        @NotNull
        Integer isForCadre,
        @NotNull
        Integer isReserved,
        @NotNull
        Integer sex,
        @NotNull
        Integer type,
        @NotNull
        Integer totalFee,
        @NotNull
        Integer selfPayFee,
        @NotNull
        Integer refundFee) {
}
