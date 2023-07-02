package edu.npu.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SendMessageDto(
        @NotNull
        String senderAdminId,
        List<String> receiverAdminIds,
        List<String> receiverUserIds,
        @NotNull
        String message
) {
}
