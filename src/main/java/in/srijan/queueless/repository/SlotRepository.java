package in.srijan.queueless.repository;

import in.srijan.queueless.entity.Slot;
import in.srijan.queueless.enums.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SlotRepository extends JpaRepository<Slot, Long> {

    boolean existsByDoctorIdAndSlotDate(Long doctorId, LocalDate date);

    List<Slot> findByDoctorIdAndSlotDateOrderByStartTime(Long doctorId, LocalDate date);

    List<Slot> findByDoctorIdAndSlotDateAndStatusOrderByStartTime(
            Long doctorId, LocalDate date, SlotStatus status);
}
