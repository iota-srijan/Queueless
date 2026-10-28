package in.srijan.queueless.notification;

import in.srijan.queueless.entity.Appointment;
import in.srijan.queueless.entity.Doctor;
import in.srijan.queueless.entity.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * v1 just logs "would email ..." - real email (spring-boot-starter-mail)
 * is a stretch goal per BLUEPRINT.md, not built here.
 */
@Service
@Profile("prod")
public class EmailNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    @Override
    public void notifyBooked(Patient patient, Doctor doctor, Appointment appointment) {
        log.info("would email {} <{}>: your appointment #{} with Dr. {} on {} at {} is booked",
                patient.getName(), patient.getEmail(), appointment.getId(),
                doctor.getName(), appointment.getAppointmentDate(), appointment.getStartTime());
    }

    @Override
    public void notifyCancelled(Patient patient, Doctor doctor, Appointment appointment) {
        log.info("would email {} <{}>: your appointment #{} with Dr. {} on {} at {} is cancelled",
                patient.getName(), patient.getEmail(), appointment.getId(),
                doctor.getName(), appointment.getAppointmentDate(), appointment.getStartTime());
    }
}
