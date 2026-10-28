package in.srijan.queueless.notification;

import in.srijan.queueless.entity.Appointment;
import in.srijan.queueless.entity.Doctor;
import in.srijan.queueless.entity.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"dev", "default"})
public class ConsoleNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(ConsoleNotificationService.class);

    @Override
    public void notifyBooked(Patient patient, Doctor doctor, Appointment appointment) {
        log.info("""

                ================ APPOINTMENT BOOKED ================
                  To:      {} <{}>
                  Doctor:  {} ({})
                  When:    {} at {}
                  Booking: #{}
                ======================================================""",
                patient.getName(), patient.getEmail(),
                doctor.getName(), doctor.getSpecialization(),
                appointment.getAppointmentDate(), appointment.getStartTime(),
                appointment.getId());
    }

    @Override
    public void notifyCancelled(Patient patient, Doctor doctor, Appointment appointment) {
        log.info("""

                ============== APPOINTMENT CANCELLED ================
                  To:      {} <{}>
                  Doctor:  {} ({})
                  Was:     {} at {}
                  Booking: #{}
                ======================================================""",
                patient.getName(), patient.getEmail(),
                doctor.getName(), doctor.getSpecialization(),
                appointment.getAppointmentDate(), appointment.getStartTime(),
                appointment.getId());
    }
}
