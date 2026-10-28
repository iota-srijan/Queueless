package in.srijan.queueless.repository;

import in.srijan.queueless.entity.Appointment;
import in.srijan.queueless.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // A2: daily booking limit
    long countByPatientIdAndAppointmentDateAndStatus(
            Long patientId, LocalDate date, AppointmentStatus status);

    // A3: same date+time clash, regardless of doctor
    boolean existsByPatientIdAndAppointmentDateAndStartTimeAndStatus(
            Long patientId, LocalDate date, LocalTime time, AppointmentStatus status);

    // D4: doctor has a future booked appointment
    boolean existsByDoctorIdAndStatusAndAppointmentDateGreaterThanEqual(
            Long doctorId, AppointmentStatus status, LocalDate from);

    List<Appointment> findByPatientIdOrderByAppointmentDateDescStartTimeDesc(Long patientId);

    List<Appointment> findByDoctorIdAndAppointmentDateOrderByStartTime(Long doctorId, LocalDate date);
}
