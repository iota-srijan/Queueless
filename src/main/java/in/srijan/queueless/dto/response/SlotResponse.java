package in.srijan.queueless.dto.response;

import in.srijan.queueless.entity.Slot;
import in.srijan.queueless.enums.SlotStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public class SlotResponse {

    private final Long id;
    private final Long doctorId;
    private final LocalDate slotDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final SlotStatus status;

    public SlotResponse(Long id, Long doctorId, LocalDate slotDate, LocalTime startTime,
                         LocalTime endTime, SlotStatus status) {
        this.id = id;
        this.doctorId = doctorId;
        this.slotDate = slotDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public static SlotResponse from(Slot slot) {
        return new SlotResponse(
                slot.getId(),
                slot.getDoctorId(),
                slot.getSlotDate(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getStatus()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public LocalDate getSlotDate() {
        return slotDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public SlotStatus getStatus() {
        return status;
    }
}
