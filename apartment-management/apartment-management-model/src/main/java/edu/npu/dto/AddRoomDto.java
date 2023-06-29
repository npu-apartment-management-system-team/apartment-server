package edu.npu.dto;

import jakarta.validation.constraints.NotNull;

public record AddRoomDto(
        @NotNull
        String apartmentId,
        @NotNull
        String name,
        @NotNull
        String usage,
        @NotNull
        Boolean isForCadre,
        @NotNull
        Boolean isReserved,
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
