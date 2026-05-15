package com.callpipeline.controller;

import com.callpipeline.dto.AppointmentRequestDto;
import com.callpipeline.model.Appointment;
import com.callpipeline.model.AppointmentStatus;
import com.callpipeline.service.AppointmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Controller
public class DashboardController {

    private final AppointmentService appointmentService;

    public DashboardController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String callerPhone, Model model) {
        List<Appointment> appointments = StringUtils.hasText(callerPhone)
                ? appointmentService.findByCallerPhone(callerPhone)
                : appointmentService.findAll();

        model.addAttribute("appointments", appointments);
        model.addAttribute("appointmentForm", new AppointmentRequestDto());
        model.addAttribute("callerPhone", callerPhone);
        model.addAttribute("statuses", AppointmentStatus.values());
        model.addAttribute("totalCount", appointments.size());
        model.addAttribute("requestedCount", countStatus(appointments, AppointmentStatus.REQUESTED));
        model.addAttribute("cancelledCount", countStatus(appointments, AppointmentStatus.CANCELLED));
        model.addAttribute("completedCount", countStatus(appointments, AppointmentStatus.COMPLETED));
        return "dashboard";
    }

    @PostMapping("/dashboard/appointments")
    public String create(@ModelAttribute AppointmentRequestDto request) {
        appointmentService.createAppointment(request);
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/appointments/{id}/status")
    public String updateStatus(
            @PathVariable UUID id,
            @RequestParam String callerPhone,
            @RequestParam AppointmentStatus status
    ) {
        if (status == AppointmentStatus.CANCELLED) {
            appointmentService.cancelAppointment(id, callerPhone);
            return "redirect:/dashboard";
        }
        AppointmentRequestDto request = new AppointmentRequestDto();
        request.setStatus(status);
        appointmentService.updateAppointmentForCaller(id, callerPhone, request);
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/appointments/{id}/cancel")
    public String cancel(@PathVariable UUID id, @RequestParam String callerPhone) {
        appointmentService.cancelAppointment(id, callerPhone);
        return "redirect:/dashboard";
    }

    private long countStatus(List<Appointment> appointments, AppointmentStatus status) {
        return appointments.stream()
                .filter(appointment -> appointment.getStatus() == status)
                .count();
    }
}
