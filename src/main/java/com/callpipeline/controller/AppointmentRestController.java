package com.callpipeline.controller;

import com.callpipeline.dto.AppointmentRequestDto;
import com.callpipeline.dto.AppointmentResponseDto;
import com.callpipeline.model.Appointment;
import com.callpipeline.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentRestController {

    private final AppointmentService appointmentService;

    public AppointmentRestController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<AppointmentResponseDto> list(@RequestParam(required = false) String callerPhone) {
        List<Appointment> appointments = callerPhone == null
                ? appointmentService.findAll()
                : appointmentService.findByCallerPhone(callerPhone);

        return appointments.stream()
                .map(AppointmentResponseDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    public AppointmentResponseDto get(@PathVariable UUID id, @RequestParam String callerPhone) {
        return AppointmentResponseDto.from(appointmentService.getAppointmentForCaller(id, callerPhone));
    }

    @PostMapping
    public AppointmentResponseDto create(@Valid @RequestBody AppointmentRequestDto request) {
        return AppointmentResponseDto.from(appointmentService.createAppointment(request));
    }

    @PutMapping("/{id}")
    public AppointmentResponseDto update(
            @PathVariable UUID id,
            @RequestParam String callerPhone,
            @RequestBody AppointmentRequestDto request
    ) {
        return AppointmentResponseDto.from(appointmentService.updateAppointmentForCaller(id, callerPhone, request));
    }

    @PostMapping("/{id}/cancel")
    public AppointmentResponseDto cancel(@PathVariable UUID id, @RequestParam String callerPhone) {
        return AppointmentResponseDto.from(appointmentService.cancelAppointment(id, callerPhone));
    }
}
