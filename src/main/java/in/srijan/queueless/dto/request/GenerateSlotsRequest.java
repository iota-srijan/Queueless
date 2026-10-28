package in.srijan.queueless.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class GenerateSlotsRequest {

    @NotNull(message = "date is required")
    private LocalDate date;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
