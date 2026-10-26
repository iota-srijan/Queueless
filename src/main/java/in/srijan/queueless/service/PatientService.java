package in.srijan.queueless.service;

import in.srijan.queueless.dto.request.RegisterPatientRequest;
import in.srijan.queueless.dto.response.PatientResponse;
import in.srijan.queueless.entity.Patient;
import in.srijan.queueless.exception.ConflictException;
import in.srijan.queueless.exception.ResourceNotFoundException;
import in.srijan.queueless.repository.PatientRepository;
import org.springframework.stereotype.Service;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
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

    public PatientResponse getById(Long id) {
        // TODO: P2 - once CurrentRequest/RoleInterceptor exist (M9), only the
        // patient themselves or a RECEPTIONIST may read this; throw 403
        // NOT_YOUR_RESOURCE otherwise. Open to everyone until then.

        Patient patient = patientRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient " + id + " not found"));
        return PatientResponse.from(patient);
    }
}
