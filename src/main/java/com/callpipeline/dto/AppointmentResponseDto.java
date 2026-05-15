package com.callpipeline.dto;

import com.callpipeline.model.Appointment;
import com.callpipeline.model.AppointmentIntent;
import com.callpipeline.model.AppointmentStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponseDto(
        UUID id,
        String callerName,
        String callerPhone,
        String callerEmail,
        String reason,
        String preferredDateTime,
        LocalDateTime scheduledAt,
        AppointmentStatus status,
        AppointmentIntent lastIntent,
        String livekitJobId,
        String roomId,
        String roomName,
        String conversationSummary,
        Instant createdAt,
        Instant updatedAt,
        Instant cancelledAt
) {
    public static AppointmentResponseDto from(Appointment appointment) {
        return new AppointmentResponseDto(
                appointment.getId(),
                appointment.getCallerName(),
                appointment.getCallerPhone(),
                appointment.getCallerEmail(),
                appointment.getReason(),
                appointment.getPreferredDateTime(),
                appointment.getScheduledAt(),
                appointment.getStatus(),
                appointment.getLastIntent(),
                appointment.getLivekitJobId(),
                appointment.getRoomId(),
                appointment.getRoomName(),
                appointment.getConversationSummary(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt(),
                appointment.getCancelledAt()
        );
    }
}
