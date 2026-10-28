package in.srijan.queueless.dto.request;

import jakarta.validation.constraints.NotNull;

public class BookAppointmentRequest {

    @NotNull(message = "slotId is required")
    private Long slotId;

    private String reason;

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
