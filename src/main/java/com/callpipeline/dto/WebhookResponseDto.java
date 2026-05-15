package com.callpipeline.dto;

import java.util.UUID;

public record WebhookResponseDto(
        String operation,
        String message,
        UUID appointmentId,
        AppointmentResponseDto appointment
) {
}
