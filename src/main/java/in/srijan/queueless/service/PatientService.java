package in.srijan.queueless.service;

import in.srijan.queueless.dto.request.RegisterPatientRequest;
import in.srijan.queueless.dto.response.AppointmentResponse;
import in.srijan.queueless.dto.response.PatientResponse;
import in.srijan.queueless.entity.Appointment;
import in.srijan.queueless.entity.Doctor;
import in.srijan.queueless.entity.Patient;
import in.srijan.queueless.exception.ConflictException;
import in.srijan.queueless.exception.ForbiddenException;
import in.srijan.queueless.exception.ResourceNotFoundException;
import in.srijan.queueless.repository.AppointmentRepository;
import in.srijan.queueless.repository.DoctorRepository;
import in.srijan.queueless.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;

    public PatientService(PatientRepository patientRepository,
                           AppointmentRepository appointmentRepository,
                           DoctorRepository doctorRepository) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
    }

    // P2: patient reads own profile only, receptionist reads any
    private void checkOwnerOrReceptionist(Long id, String callerRole, Long callerId) {
        boolean isReceptionist = "RECEPTIONIST".equals(callerRole);
        boolean isSelf = "PATIENT".equals(callerRole) && id.equals(callerId);
        if (!isReceptionist && !isSelf) {
            throw new ForbiddenException("NOT_YOUR_RESOURCE",
                    "You may only access your own patient record");
        }
    }

    public PatientResponse register(RegisterPatientRequest request) {
        // P1: duplicate email -> 409
        if (patientRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("DUPLICATE_EMAIL",
                    "A patient with email " + request.getEmail() + " already exists");
        }

        Patient patient = new Patient();
        patient.setName(request.getName());
        patient.setEmail(request.getEmail());
        patient.setPhone(request.getPhone());
        patient.setDateOfBirth(request.getDateOfBirth());

        Patient saved = patientRepository.save(patient);
        return PatientResponse.from(saved);
    }

    public PatientResponse getById(Long id, String callerRole, Long callerId) {
        checkOwnerOrReceptionist(id, callerRole, callerId);

        Patient patient = patientRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient " + id + " not found"));
        return PatientResponse.from(patient);
    }

    // P2: same ownership rule as getById
    public List<AppointmentResponse> getAppointments(Long id, String callerRole, Long callerId) {
        checkOwnerOrReceptionist(id, callerRole, callerId);

        patientRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient " + id + " not found"));

        List<Appointment> appointments =
                appointmentRepository.findByPatientIdOrderByAppointmentDateDescStartTimeDesc(id);

        // N+1 here: one doctor lookup per appointment. Known v1 limitation (BLUEPRINT §13) -
        // fixed properly by @ManyToOne/@EntityGraph in v2 (M12).
        return appointments.stream()
                .map(appointment -> {
                    Doctor doctor = doctorRepository.findById(appointment.getDoctorId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Doctor " + appointment.getDoctorId() + " not found"));
                    return AppointmentResponse.from(appointment, doctor);
                })
                .toList();
    }
}
