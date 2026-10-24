package in.srijan.queueless.repository;

import in.srijan.queueless.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    boolean existsByEmail(String email);

    List<Doctor> findByActiveTrue();

    List<Doctor> findBySpecializationIgnoreCaseAndActiveTrue(String specialization);

    Optional<Doctor> findByIdAndActiveTrue(Long id);
}
