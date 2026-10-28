package in.srijan.queueless.service;

import in.srijan.queueless.dto.request.CreateDoctorRequest;
import in.srijan.queueless.dto.request.UpdateDoctorRequest;
import in.srijan.queueless.dto.response.DoctorResponse;
import in.srijan.queueless.entity.Doctor;
import in.srijan.queueless.enums.AppointmentStatus;
import in.srijan.queueless.exception.ConflictException;
import in.srijan.queueless.exception.ResourceNotFoundException;
import in.srijan.queueless.repository.AppointmentRepository;
import in.srijan.queueless.repository.DoctorRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    public DoctorService(DoctorRepository doctorRepository, AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public DoctorResponse create(CreateDoctorRequest request) {
        // D2: duplicate email -> 409
        if (doctorRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("DUPLICATE_EMAIL",
                    "A doctor with email " + request.getEmail() + " already exists");
        }

        // D1: required fields
        Doctor doctor = new Doctor();
        doctor.setName(request.getName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setEmail(request.getEmail());
        doctor.setPhone(request.getPhone());
        doctor.setConsultationFee(request.getConsultationFee());

        Doctor saved = doctorRepository.save(doctor);
        return DoctorResponse.from(saved);
    }

    public List<DoctorResponse> list(String specialization) {
        // D5: inactive doctors never appear in listings
        List<Doctor> doctors = (specialization == null || specialization.isBlank())
                ? doctorRepository.findByActiveTrue()
                : doctorRepository.findBySpecializationIgnoreCaseAndActiveTrue(specialization);

        return doctors.stream().map(DoctorResponse::from).toList();
    }

    public DoctorResponse getById(Long id) {
        // D5: an inactive doctor looks "not found" to everyone
        Doctor doctor = doctorRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor " + id + " not found"));
        return DoctorResponse.from(doctor);
    }

    public DoctorResponse update(Long id, UpdateDoctorRequest request) {
        Doctor doctor = doctorRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor " + id + " not found"));

        doctor.setName(request.getName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setPhone(request.getPhone());
        doctor.setConsultationFee(request.getConsultationFee());

        Doctor saved = doctorRepository.save(doctor);
        return DoctorResponse.from(saved);
    }

    public void deactivate(Long id) {
        Doctor doctor = doctorRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor " + id + " not found"));

        // D4: can't deactivate a doctor with a future BOOKED appointment
        if (appointmentRepository.existsByDoctorIdAndStatusAndAppointmentDateGreaterThanEqual(
                id, AppointmentStatus.BOOKED, LocalDate.now())) {
            throw new ConflictException("DOCTOR_HAS_UPCOMING_APPOINTMENTS",
                    "Doctor " + id + " has upcoming appointments and cannot be deactivated");
        }

        // D3: soft delete only
        doctor.setActive(false);
        doctorRepository.save(doctor);
    }
}
