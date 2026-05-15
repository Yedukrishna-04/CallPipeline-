package com.callpipeline.service;

import com.callpipeline.dto.AppointmentResponseDto;
import com.callpipeline.dto.LiveKitWebhookPayload;
import com.callpipeline.dto.WebhookResponseDto;
import com.callpipeline.model.Appointment;
import com.callpipeline.model.AppointmentIntent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LiveKitWebhookService {

    private final ObjectMapper objectMapper;
    private final WebhookPayloadExtractor payloadExtractor;
    private final AppointmentService appointmentService;

    public LiveKitWebhookService(
            ObjectMapper objectMapper,
            WebhookPayloadExtractor payloadExtractor,
            AppointmentService appointmentService
    ) {
        this.objectMapper = objectMapper;
        this.payloadExtractor = payloadExtractor;
        this.appointmentService = appointmentService;
    }

    @Transactional
    public WebhookResponseDto process(JsonNode rawPayload) {
        LiveKitWebhookPayload payload = objectMapper.convertValue(rawPayload, LiveKitWebhookPayload.class);
        ExtractedAppointmentData data = payloadExtractor.extract(payload, rawPayload);
        String rawJson = toRawJson(rawPayload);

        AppointmentIntent intent = data.intent();
        if (intent == AppointmentIntent.UNKNOWN && data.hasCreateDetails()) {
            intent = AppointmentIntent.CREATE_APPOINTMENT;
        }

        return switch (intent) {
            case CREATE_APPOINTMENT -> create(payload, data, rawJson);
            case UPDATE_APPOINTMENT -> update(payload, data, rawJson);
            case CANCEL_APPOINTMENT -> cancel(payload, data, rawJson);
            case FETCH_APPOINTMENT -> fetch(data);
            case UNKNOWN -> new WebhookResponseDto(
                    AppointmentIntent.UNKNOWN.name(),
                    "No appointment operation was detected in the webhook payload.",
                    null,
                    null
            );
        };
    }

    private WebhookResponseDto create(LiveKitWebhookPayload payload, ExtractedAppointmentData data, String rawJson) {
        Appointment appointment = appointmentService.createFromWebhook(
                data,
                payload.getJobId(),
                payload.getRoomId(),
                payload.getRoomName(),
                rawJson
        );
        return new WebhookResponseDto(
                AppointmentIntent.CREATE_APPOINTMENT.name(),
                "Appointment request created.",
                appointment.getId(),
                AppointmentResponseDto.from(appointment)
        );
    }

    private WebhookResponseDto update(LiveKitWebhookPayload payload, ExtractedAppointmentData data, String rawJson) {
        Optional<Appointment> target = appointmentService.findWebhookTarget(data.appointmentId(), data.callerPhone());
        if (target.isEmpty()) {
            return notFound(AppointmentIntent.UPDATE_APPOINTMENT);
        }
        Appointment appointment = appointmentService.updateFromWebhook(
                target.get(),
                data,
                payload.getJobId(),
                payload.getRoomId(),
                payload.getRoomName(),
                rawJson
        );
        return new WebhookResponseDto(
                AppointmentIntent.UPDATE_APPOINTMENT.name(),
                "Appointment updated.",
                appointment.getId(),
                AppointmentResponseDto.from(appointment)
        );
    }

    private WebhookResponseDto cancel(LiveKitWebhookPayload payload, ExtractedAppointmentData data, String rawJson) {
        Optional<Appointment> target = appointmentService.findWebhookTarget(data.appointmentId(), data.callerPhone());
        if (target.isEmpty()) {
            return notFound(AppointmentIntent.CANCEL_APPOINTMENT);
        }
        Appointment appointment = appointmentService.cancelFromWebhook(
                target.get(),
                data,
                payload.getJobId(),
                payload.getRoomId(),
                payload.getRoomName(),
                rawJson
        );
        return new WebhookResponseDto(
                AppointmentIntent.CANCEL_APPOINTMENT.name(),
                "Appointment cancelled.",
                appointment.getId(),
                AppointmentResponseDto.from(appointment)
        );
    }

    private WebhookResponseDto fetch(ExtractedAppointmentData data) {
        Optional<Appointment> target = appointmentService.findWebhookTarget(data.appointmentId(), data.callerPhone());
        return target
                .map(appointment -> new WebhookResponseDto(
                        AppointmentIntent.FETCH_APPOINTMENT.name(),
                        "Appointment details found.",
                        appointment.getId(),
                        AppointmentResponseDto.from(appointment)
                ))
                .orElseGet(() -> notFound(AppointmentIntent.FETCH_APPOINTMENT));
    }

    private WebhookResponseDto notFound(AppointmentIntent intent) {
        return new WebhookResponseDto(
                intent.name(),
                "No matching appointment was found. Provide caller_phone and, when available, appointment_id.",
                null,
                null
        );
    }

    private String toRawJson(JsonNode rawPayload) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rawPayload);
        } catch (JsonProcessingException ignored) {
            return rawPayload.toString();
        }
    }
}
