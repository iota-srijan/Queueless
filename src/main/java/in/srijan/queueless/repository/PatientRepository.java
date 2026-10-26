package in.srijan.queueless.repository;

import in.srijan.queueless.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    boolean existsByEmail(String email);

    Optional<Patient> findByIdAndDeletedFalse(Long id);
}
