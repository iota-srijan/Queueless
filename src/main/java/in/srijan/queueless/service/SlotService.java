package in.srijan.queueless.service;

import in.srijan.queueless.config.ClinicProperties;
import in.srijan.queueless.dto.response.SlotResponse;
import in.srijan.queueless.entity.Doctor;
import in.srijan.queueless.entity.Slot;
import in.srijan.queueless.enums.SlotStatus;
import in.srijan.queueless.exception.ConflictException;
import in.srijan.queueless.exception.ResourceNotFoundException;
import in.srijan.queueless.exception.UnprocessableEntityException;
import in.srijan.queueless.repository.DoctorRepository;
import in.srijan.queueless.repository.SlotRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SlotService {

    private final SlotRepository slotRepository;
    private final DoctorRepository doctorRepository;
    private final ClinicProperties clinicProperties;
    private final HolidayCalendar holidayCalendar;

    public SlotService(SlotRepository slotRepository, DoctorRepository doctorRepository,
                        ClinicProperties clinicProperties, HolidayCalendar holidayCalendar) {
        this.slotRepository = slotRepository;
        this.doctorRepository = doctorRepository;
        this.clinicProperties = clinicProperties;
        this.holidayCalendar = holidayCalendar;
    }

    public List<SlotResponse> generate(Long doctorId, LocalDate date) {
        // TODO: role check - RECEPTIONIST only (needs M9's RoleInterceptor)

        // D5: inactive doctor can't get new slots (findByIdAndActiveTrue -> "not found" otherwise)
        Doctor doctor = doctorRepository.findByIdAndActiveTrue(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor " + doctorId + " not found"));

        // S2: can't generate for a past date
        if (date.isBefore(LocalDate.now())) {
            throw new UnprocessableEntityException("SLOT_IN_PAST",
                    "Cannot generate slots for a past date: " + date);
        }

        // S2: can't generate on a clinic holiday
        if (holidayCalendar.isHoliday(date)) {
            throw new UnprocessableEntityException("CLINIC_HOLIDAY",
                    "Clinic is closed on " + date);
        }

        // S2: can't generate twice for the same doctor + date
        if (slotRepository.existsByDoctorIdAndSlotDate(doctorId, date)) {
            throw new ConflictException("SLOTS_ALREADY_GENERATED",
                    "Slots already exist for doctor " + doctorId + " on " + date);
        }

        // S1: build one slot per step from opening to closing, skipping lunch
        List<Slot> slots = new ArrayList<>();
        LocalTime openingTime = clinicProperties.getOpeningTime();
        LocalTime closingTime = clinicProperties.getClosingTime();
        LocalTime lunchStart = clinicProperties.getLunchStart();
        LocalTime lunchEnd = clinicProperties.getLunchEnd();
        int durationMinutes = clinicProperties.getSlotDurationMinutes();

        LocalTime start = openingTime;
        while (!start.plusMinutes(durationMinutes).isAfter(closingTime)) {
            LocalTime end = start.plusMinutes(durationMinutes);
            boolean inLunch = !start.isBefore(lunchStart) && start.isBefore(lunchEnd);

            if (!inLunch) {
                Slot slot = new Slot();
                slot.setDoctorId(doctorId);
                slot.setSlotDate(date);
                slot.setStartTime(start);
                slot.setEndTime(end);
                slot.setStatus(SlotStatus.AVAILABLE);
                slots.add(slot);
            }

            start = end;
        }

        List<Slot> saved = slotRepository.saveAll(slots);
        return saved.stream().map(SlotResponse::from).toList();
    }

    public List<SlotResponse> list(Long doctorId, LocalDate date, SlotStatus status) {
        List<Slot> slots = (status == null)
                ? slotRepository.findByDoctorIdAndSlotDateOrderByStartTime(doctorId, date)
                : slotRepository.findByDoctorIdAndSlotDateAndStatusOrderByStartTime(doctorId, date, status);

        return slots.stream().map(SlotResponse::from).toList();
    }

    public SlotResponse block(Long id) {
        // TODO: role check - RECEPTIONIST only (needs M9's RoleInterceptor)

        Slot slot = slotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Slot " + id + " not found"));

        // S4: can only block a slot that is currently AVAILABLE
        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new ConflictException("INVALID_STATUS_TRANSITION",
                    "Slot " + id + " is " + slot.getStatus() + ", cannot block it");
        }

        slot.setStatus(SlotStatus.BLOCKED);
        Slot saved = slotRepository.save(slot);
        return SlotResponse.from(saved);
    }
}
