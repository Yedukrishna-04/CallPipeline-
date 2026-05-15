package com.callpipeline.repository;

import com.callpipeline.model.Appointment;
import com.callpipeline.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    List<Appointment> findByCallerPhoneOrderByCreatedAtDesc(String callerPhone);

    Optional<Appointment> findFirstByCallerPhoneAndStatusNotOrderByCreatedAtDesc(
            String callerPhone,
            AppointmentStatus status
    );

    Optional<Appointment> findFirstByIdAndCallerPhone(UUID id, String callerPhone);
}
