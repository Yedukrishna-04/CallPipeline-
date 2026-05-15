package com.callpipeline.service;

import com.callpipeline.dto.AppointmentRequestDto;
import com.callpipeline.model.Appointment;
import com.callpipeline.model.AppointmentIntent;
import com.callpipeline.model.AppointmentStatus;
import com.callpipeline.repository.AppointmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Appointment> findAll() {
        return appointmentRepository.findAll()
                .stream()
                .sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Appointment> findByCallerPhone(String callerPhone) {
        if (!StringUtils.hasText(callerPhone)) {
            return List.of();
        }
        return appointmentRepository.findByCallerPhoneOrderByCreatedAtDesc(callerPhone.trim());
    }

    @Transactional(readOnly = true)
    public Appointment getAppointmentForCaller(UUID id, String callerPhone) {
        requireCallerPhone(callerPhone);
        return appointmentRepository.findFirstByIdAndCallerPhone(id, callerPhone.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found for caller"));
    }

    @Transactional(readOnly = true)
    public Optional<Appointment> findWebhookTarget(UUID appointmentId, String callerPhone) {
        if (appointmentId != null && StringUtils.hasText(callerPhone)) {
            return appointmentRepository.findFirstByIdAndCallerPhone(appointmentId, callerPhone.trim());
        }
        if (StringUtils.hasText(callerPhone)) {
            return appointmentRepository.findFirstByCallerPhoneAndStatusNotOrderByCreatedAtDesc(
                    callerPhone.trim(),
                    AppointmentStatus.CANCELLED
            );
        }
        return Optional.empty();
    }

    @Transactional
    public Appointment createAppointment(AppointmentRequestDto request) {
        Appointment appointment = new Appointment();
        appointment.setLastIntent(AppointmentIntent.CREATE_APPOINTMENT);
        appointment.setStatus(request.getStatus() == null ? AppointmentStatus.REQUESTED : request.getStatus());
        applyRequest(appointment, request, true);
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment updateAppointmentForCaller(UUID id, String callerPhone, AppointmentRequestDto request) {
        Appointment appointment = getAppointmentForCaller(id, callerPhone);
        appointment.setLastIntent(AppointmentIntent.UPDATE_APPOINTMENT);
        appointment.setStatus(request.getStatus() == null ? AppointmentStatus.UPDATED : request.getStatus());
        applyRequest(appointment, request, false);
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment cancelAppointment(UUID id, String callerPhone) {
        Appointment appointment = getAppointmentForCaller(id, callerPhone);
        return cancelAppointment(appointment);
    }

    @Transactional
    public Appointment createFromWebhook(ExtractedAppointmentData data, String jobId, String roomId, String roomName, String rawPayload) {
        Appointment appointment = new Appointment();
        appointment.setCallerName(defaultValue(data.callerName(), "Unknown caller"));
        appointment.setCallerPhone(defaultValue(data.callerPhone(), "Unknown phone"));
        appointment.setCallerEmail(emptyToNull(data.callerEmail()));
        appointment.setReason(defaultValue(data.reason(), "Appointment request"));
        appointment.setPreferredDateTime(emptyToNull(data.preferredDateTime()));
        appointment.setScheduledAt(data.scheduledAt());
        appointment.setStatus(AppointmentStatus.REQUESTED);
        appointment.setLastIntent(AppointmentIntent.CREATE_APPOINTMENT);
        appointment.setConversationSummary(emptyToNull(data.summary()));
        applyWebhookContext(appointment, jobId, roomId, roomName, rawPayload);
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment updateFromWebhook(Appointment appointment, ExtractedAppointmentData data, String jobId, String roomId, String roomName, String rawPayload) {
        updateIfPresent(data.callerName(), appointment::setCallerName);
        updateIfPresent(data.callerEmail(), appointment::setCallerEmail);
        updateIfPresent(data.reason(), appointment::setReason);
        updateIfPresent(data.preferredDateTime(), appointment::setPreferredDateTime);
        if (data.scheduledAt() != null) {
            appointment.setScheduledAt(data.scheduledAt());
        }
        updateIfPresent(data.summary(), appointment::setConversationSummary);
        appointment.setStatus(AppointmentStatus.UPDATED);
        appointment.setLastIntent(AppointmentIntent.UPDATE_APPOINTMENT);
        applyWebhookContext(appointment, jobId, roomId, roomName, rawPayload);
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment cancelFromWebhook(Appointment appointment, ExtractedAppointmentData data, String jobId, String roomId, String roomName, String rawPayload) {
        updateIfPresent(data.summary(), appointment::setConversationSummary);
        appointment.setLastIntent(AppointmentIntent.CANCEL_APPOINTMENT);
        applyWebhookContext(appointment, jobId, roomId, roomName, rawPayload);
        return cancelAppointment(appointment);
    }

    private Appointment cancelAppointment(Appointment appointment) {
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledAt(Instant.now());
        return appointmentRepository.save(appointment);
    }

    private void applyRequest(Appointment appointment, AppointmentRequestDto request, boolean requireDefaults) {
        if (requireDefaults) {
            appointment.setCallerName(defaultValue(request.getCallerName(), "Unknown caller"));
            appointment.setCallerPhone(defaultValue(request.getCallerPhone(), "Unknown phone"));
            appointment.setReason(defaultValue(request.getReason(), "Appointment request"));
        } else {
            updateIfPresent(request.getCallerName(), appointment::setCallerName);
            updateIfPresent(request.getReason(), appointment::setReason);
        }

        updateIfPresent(request.getCallerEmail(), appointment::setCallerEmail);
        updateIfPresent(request.getPreferredDateTime(), appointment::setPreferredDateTime);
        updateIfPresent(request.getConversationSummary(), appointment::setConversationSummary);
        if (request.getScheduledAt() != null) {
            appointment.setScheduledAt(request.getScheduledAt());
        }
    }

    private void applyWebhookContext(Appointment appointment, String jobId, String roomId, String roomName, String rawPayload) {
        updateIfPresent(jobId, appointment::setLivekitJobId);
        updateIfPresent(roomId, appointment::setRoomId);
        updateIfPresent(roomName, appointment::setRoomName);
        updateIfPresent(rawPayload, appointment::setRawWebhookPayload);
    }

    private void requireCallerPhone(String callerPhone) {
        if (!StringUtils.hasText(callerPhone)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "callerPhone is required for user-specific appointment access");
        }
    }

    private String defaultValue(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private String emptyToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private void updateIfPresent(String value, java.util.function.Consumer<String> setter) {
        if (StringUtils.hasText(value)) {
            setter.accept(value.trim());
        }
    }
}
