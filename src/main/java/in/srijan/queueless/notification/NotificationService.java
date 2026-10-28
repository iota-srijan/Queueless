package in.srijan.queueless.notification;

import in.srijan.queueless.entity.Appointment;
import in.srijan.queueless.entity.Doctor;
import in.srijan.queueless.entity.Patient;

public interface NotificationService {

    void notifyBooked(Patient patient, Doctor doctor, Appointment appointment);

    void notifyCancelled(Patient patient, Doctor doctor, Appointment appointment);
}
