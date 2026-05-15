package com.callpipeline.service;

import com.callpipeline.model.AppointmentIntent;

import java.time.LocalDateTime;
import java.util.UUID;

public record ExtractedAppointmentData(
        AppointmentIntent intent,
        String rawIntent,
        UUID appointmentId,
        String callerName,
        String callerPhone,
        String callerEmail,
        String reason,
        String preferredDateTime,
        LocalDateTime scheduledAt,
        String summary
) {
    boolean hasCreateDetails() {
        return hasText(callerName) || hasText(callerPhone) || hasText(reason) || hasText(preferredDateTime);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
