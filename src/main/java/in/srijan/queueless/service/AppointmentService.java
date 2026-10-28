package in.srijan.queueless.service;

import in.srijan.queueless.config.ClinicProperties;
import in.srijan.queueless.dto.request.BookAppointmentRequest;
import in.srijan.queueless.dto.request.CancelAppointmentRequest;
import in.srijan.queueless.dto.response.AppointmentResponse;
import in.srijan.queueless.entity.Appointment;
import in.srijan.queueless.entity.Doctor;
import in.srijan.queueless.entity.Patient;
import in.srijan.queueless.entity.Slot;
import in.srijan.queueless.enums.AppointmentStatus;
import in.srijan.queueless.enums.SlotStatus;
import in.srijan.queueless.exception.ConflictException;
import in.srijan.queueless.exception.ForbiddenException;
import in.srijan.queueless.exception.ResourceNotFoundException;
import in.srijan.queueless.exception.UnprocessableEntityException;
import in.srijan.queueless.notification.NotificationService;
import in.srijan.queueless.repository.AppointmentRepository;
import in.srijan.queueless.repository.DoctorRepository;
import in.srijan.queueless.repository.PatientRepository;
import in.srijan.queueless.repository.SlotRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final SlotRepository slotRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ClinicProperties clinicProperties;
    private final NotificationService notificationService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              SlotRepository slotRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              ClinicProperties clinicProperties,
                              NotificationService notificationService) {
        this.appointmentRepository = appointmentRepository;
        this.slotRepository = slotRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.clinicProperties = clinicProperties;
        this.notificationService = notificationService;
    }

    public AppointmentResponse book(
            Long patientId,
            BookAppointmentRequest request) {



        Patient patient = patientRepository
                .findByIdAndDeletedFalse(patientId).orElseThrow(
                        () -> new ResourceNotFoundException("Patient "
                                + patientId +
                                " not found"));

        Slot slot = slotRepository.findById(
                request.getSlotId()).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Slot " + request.getSlotId() + " not found")
        );


        if (slot.getStatus()!= SlotStatus.AVAILABLE){
            throw new ConflictException("SLOT_NOT_AVAILABLE",
                    "Slot " + slot.getId() + " is not available");
        }


        if (!LocalDateTime.of
                (slot.getSlotDate(), slot.getStartTime())
                .isAfter(LocalDateTime.now())){
            throw new UnprocessableEntityException("SLOT_IN_PAST",
                    "Slot " + slot.getId()
                            + " starts at "
                            + slot.getStartTime()
                            + " on " +
                            slot.getSlotDate() +
                            ", which is in the past");
        }

        Doctor doctor = doctorRepository
                .findByIdAndActiveTrue(slot.getDoctorId())
                .orElseThrow(() -> new ConflictException("SLOT_NOT_AVAILABLE",
                        "Slot " +
                                slot.getId() +
                                " is not available"));

        if (appointmentRepository.
                countByPatientIdAndAppointmentDateAndStatus
                        (patientId, slot.getSlotDate(),
                                AppointmentStatus.BOOKED)>=
                        clinicProperties.getMaxBookedPerDay()){
            throw new UnprocessableEntityException("DAILY_LIMIT_REACHED", "Maximum number " +
                    "of appointments  made (2) has reached," +
                    " try again tomorrow");
        }

        if (appointmentRepository
                .existsByPatientIdAndAppointmentDateAndStartTimeAndStatus
                        (patientId, slot.getSlotDate(),
                                slot.getStartTime(),
                        AppointmentStatus.BOOKED)){
             throw new UnprocessableEntityException("TIME_CLASH", "Slot timing is clashing with another patient's slot timing");
        }

        slot.setStatus(SlotStatus.BOOKED);
        slotRepository.save(slot);

        Appointment appointment = new Appointment();

        appointment.setSlotId(slot.getId());
        appointment.setDoctorId(slot.getDoctorId());
        appointment.setPatientId(patientId);
        appointment.setAppointmentDate(slot.getSlotDate());
        appointment.setStartTime(slot.getStartTime());
        appointment.setReason(request.getReason());

        Appointment savedAppointment =
                appointmentRepository.save(appointment);

        // A11: notify the patient on booking
        notificationService.notifyBooked(patient, doctor, savedAppointment);

        return AppointmentResponse.from(savedAppointment,
                doctor,"" +
                        "Appointment booked successfully!");
    }


    public AppointmentResponse cancel(Long appointmentId,
                                      String callerRole,
                                      Long callerId,
                                      CancelAppointmentRequest request){

        Appointment appointment = appointmentRepository
                .findById(appointmentId).orElseThrow(
                        () -> new ResourceNotFoundException("Appointment "
                                + appointmentId +
                                " not found"));

        if (!Objects.equals(callerRole, "RECEPTIONIST")) {
            if (!Objects.equals(appointment.getPatientId(), callerId)) {
                throw new ForbiddenException(
                        "NOT_YOUR_RESOURCE", "You dont have" +
                        " the authorization to cancel someone else's " +
                        "appointment.");
            }
        }

        if (!appointment.getStatus().
                canMoveTo(AppointmentStatus.CANCELLED)){

            throw new ConflictException
                    ("INVALID_STATUS_TRANSITION",
                    "Appointment " + appointmentId +
                            " is " +
                            appointment.getStatus()
                            + ", cannot cancel it");
        }

        if (!Objects.equals(callerRole, "RECEPTIONIST")) {
            LocalDateTime appointmentStart = LocalDateTime.of(
                    appointment.getAppointmentDate(), appointment.getStartTime());
            LocalDateTime cutoff = LocalDateTime.now()
                    .plusHours(clinicProperties.getCancellationWindowHours());
            if (appointmentStart.isBefore(cutoff)) {
                throw new UnprocessableEntityException("CANCELLATION_WINDOW_PASSED",
                        "Cannot cancel within " + clinicProperties.getCancellationWindowHours()
                                + " hours of the appointment");
            }
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledBy(callerRole);
        appointment.setCancelReason(request.getReason());
        appointmentRepository.save(appointment);

        Slot slot = slotRepository.findById(appointment.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Slot " + appointment.getSlotId() + " not found"));
        slot.setStatus(SlotStatus.AVAILABLE);
        slotRepository.save(slot);

        Doctor doctor = doctorRepository.findByIdAndActiveTrue(appointment.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor " + appointment.getDoctorId() + " not found"));

        // A11: notify the patient on cancellation
        Patient patient = patientRepository.findByIdAndDeletedFalse(appointment.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient " + appointment.getPatientId() + " not found"));
        notificationService.notifyCancelled(patient, doctor, appointment);

        return AppointmentResponse.from(appointment, doctor, "Appointment cancelled");
    }

    public AppointmentResponse complete(Long appointmentId, String callerRole, Long callerId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment " + appointmentId + " not found"));

        // A8: only the doctor who owns this appointment can complete it
        if (!Objects.equals(callerRole, "DOCTOR") || !Objects.equals(appointment.getDoctorId(), callerId)) {
            throw new ForbiddenException("NOT_YOUR_RESOURCE",
                    "You may only complete your own appointments");
        }

        // A5
        if (!appointment.getStatus().canMoveTo(AppointmentStatus.COMPLETED)) {
            throw new ConflictException("INVALID_STATUS_TRANSITION",
                    "Appointment " + appointmentId + " is " + appointment.getStatus()
                            + ", cannot complete it");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        Appointment saved = appointmentRepository.save(appointment);

        Doctor doctor = doctorRepository.findByIdAndActiveTrue(appointment.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor " + appointment.getDoctorId() + " not found"));

        return AppointmentResponse.from(saved, doctor, "Appointment marked completed");
    }

    public AppointmentResponse noShow(Long appointmentId, String callerRole, Long callerId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment " + appointmentId + " not found"));

        // A8: only the doctor who owns this appointment can mark it no-show
        if (!Objects.equals(callerRole, "DOCTOR") || !Objects.equals(appointment.getDoctorId(), callerId)) {
            throw new ForbiddenException("NOT_YOUR_RESOURCE",
                    "You may only mark your own appointments as no-show");
        }

        // A5
        if (!appointment.getStatus().canMoveTo(AppointmentStatus.NO_SHOW)) {
            throw new ConflictException("INVALID_STATUS_TRANSITION",
                    "Appointment " + appointmentId + " is " + appointment.getStatus()
                            + ", cannot mark it no-show");
        }

        appointment.setStatus(AppointmentStatus.NO_SHOW);
        Appointment saved = appointmentRepository.save(appointment);

        Doctor doctor = doctorRepository.findByIdAndActiveTrue(appointment.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor " + appointment.getDoctorId() + " not found"));

        return AppointmentResponse.from(saved, doctor, "Appointment marked as no-show");
    }
}
