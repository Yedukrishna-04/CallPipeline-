package com.callpipeline.service;

import com.callpipeline.dto.LiveKitWebhookPayload;
import com.callpipeline.model.AppointmentIntent;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class WebhookPayloadExtractor {

    public ExtractedAppointmentData extract(LiveKitWebhookPayload payload, JsonNode rawPayload) {
        String summary = firstText(
                payload.getSummary(),
                findText(rawPayload, aliases("summary", "conversation_summary", "conversationSummary", "call_summary", "callSummary"))
        );

        String rawIntent = firstText(
                payload.getIntent(),
                findText(rawPayload, aliases("intent", "user_intent", "userIntent", "operation", "appointment_intent", "appointmentIntent"))
        );

        AppointmentIntent intent = AppointmentIntent.from(rawIntent);
        if (intent == AppointmentIntent.UNKNOWN) {
            intent = AppointmentIntent.from(summary);
        }

        String appointmentIdValue = findText(rawPayload, aliases("appointment_id", "appointmentId", "booking_id", "bookingId"));
        UUID appointmentId = parseUuid(appointmentIdValue);

        String callerName = findText(rawPayload, aliases("caller_name", "callerName", "customer_name", "customerName", "patient_name", "patientName", "user_name", "userName", "name"));
        String callerPhone = findText(rawPayload, aliases("caller_phone", "callerPhone", "phone_number", "phoneNumber", "mobile_number", "mobileNumber", "contact_number", "contactNumber", "phone", "mobile"));
        String callerEmail = findText(rawPayload, aliases("caller_email", "callerEmail", "email_address", "emailAddress", "email"));
        String reason = findText(rawPayload, aliases("reason_for_visit", "reasonForVisit", "appointment_reason", "appointmentReason", "service", "reason", "issue"));
        String preferredDateTime = findText(rawPayload, aliases("preferred_date_time", "preferredDateTime", "preferred_time", "preferredTime", "appointment_time", "appointmentTime", "time"));
        String scheduledAtValue = findText(rawPayload, aliases("scheduled_at", "scheduledAt", "appointment_date_time", "appointmentDateTime", "appointment_datetime", "date_time", "dateTime"));

        return new ExtractedAppointmentData(
                intent,
                rawIntent,
                appointmentId,
                callerName,
                callerPhone,
                callerEmail,
                reason,
                preferredDateTime,
                parseDateTime(scheduledAtValue),
                summary
        );
    }

    private String findText(JsonNode node, Set<String> aliases) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }

        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                if (aliases.contains(normalize(entry.getKey()))) {
                    String value = nodeToText(entry.getValue());
                    if (StringUtils.hasText(value)) {
                        return value;
                    }
                }
            }

            fields = node.fields();
            while (fields.hasNext()) {
                String value = findText(fields.next().getValue(), aliases);
                if (StringUtils.hasText(value)) {
                    return value;
                }
            }
        }

        if (node.isArray()) {
            for (JsonNode child : node) {
                String value = findText(child, aliases);
                if (StringUtils.hasText(value)) {
                    return value;
                }
            }
        }

        return null;
    }

    private String nodeToText(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        if (node.isTextual() || node.isNumber() || node.isBoolean()) {
            return clean(node.asText());
        }
        if (node.isObject()) {
            for (String key : new String[]{"value", "text", "answer", "selected"}) {
                String value = nodeToText(node.get(key));
                if (StringUtils.hasText(value)) {
                    return value;
                }
            }
        }
        return null;
    }

    private String firstText(String... values) {
        for (String value : values) {
            String cleaned = clean(value);
            if (StringUtils.hasText(cleaned)) {
                return cleaned;
            }
        }
        return null;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private UUID parseUuid(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim());
        } catch (RuntimeException ignored) {
            try {
                return OffsetDateTime.parse(value.trim()).toLocalDateTime();
            } catch (RuntimeException secondIgnored) {
                return null;
            }
        }
    }

    private Set<String> aliases(String... values) {
        Set<String> aliases = new HashSet<>();
        Arrays.stream(values).map(this::normalize).forEach(aliases::add);
        return aliases;
    }

    private String normalize(String value) {
        return value == null
                ? ""
                : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
