package in.srijan.queueless.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import in.srijan.queueless.entity.Appointment;
import in.srijan.queueless.entity.Doctor;
import in.srijan.queueless.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppointmentResponse {

    private final Long appointmentId;
    private final String doctorName;
    private final String specialization;
    private final LocalDate date;
    private final LocalTime startTime;
    private final AppointmentStatus status;
    private final String message;

    public AppointmentResponse(Long appointmentId, String doctorName, String specialization,
                                LocalDate date, LocalTime startTime, AppointmentStatus status,
                                String message) {
        this.appointmentId = appointmentId;
        this.doctorName = doctorName;
        this.specialization = specialization;
        this.date = date;
        this.startTime = startTime;
        this.status = status;
        this.message = message;
    }

    // BLUEPRINT §4's example response needs the doctor's name/specialization,
    // which Appointment doesn't carry (only doctorId) - pass the Doctor in too.
    public static AppointmentResponse from(Appointment appointment, Doctor doctor, String message) {
        return new AppointmentResponse(
                appointment.getId(),
                doctor.getName(),
                doctor.getSpecialization(),
                appointment.getAppointmentDate(),
                appointment.getStartTime(),
                appointment.getStatus(),
                message
        );
    }

    // for listing/reading appointments where there's no action-specific message to show
    public static AppointmentResponse from(Appointment appointment, Doctor doctor) {
        return from(appointment, doctor, null);
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
