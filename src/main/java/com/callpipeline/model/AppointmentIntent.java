package com.callpipeline.model;

import java.util.Locale;

public enum AppointmentIntent {
    CREATE_APPOINTMENT,
    UPDATE_APPOINTMENT,
    CANCEL_APPOINTMENT,
    FETCH_APPOINTMENT,
    UNKNOWN;

    public static AppointmentIntent from(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }

        String normalized = value.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');

        for (AppointmentIntent intent : values()) {
            if (intent.name().equals(normalized)) {
                return intent;
            }
        }

        String loose = normalized.toLowerCase(Locale.ROOT);
        if (loose.contains("create") || loose.contains("book") || loose.contains("schedule")) {
            return CREATE_APPOINTMENT;
        }
        if (loose.contains("update") || loose.contains("reschedule") || loose.contains("change")) {
            return UPDATE_APPOINTMENT;
        }
        if (loose.contains("cancel")) {
            return CANCEL_APPOINTMENT;
        }
        if (loose.contains("fetch") || loose.contains("read") || loose.contains("detail") || loose.contains("status")) {
            return FETCH_APPOINTMENT;
        }

        return UNKNOWN;
    }
}
