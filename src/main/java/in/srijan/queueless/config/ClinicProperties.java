package in.srijan.queueless.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "clinic")
public class ClinicProperties {

    private String name;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private LocalTime lunchStart;
    private LocalTime lunchEnd;
    private int slotDurationMinutes;
    private int maxBookedPerDay;
    private int cancellationWindowHours;
    private List<LocalDate> holidays;
    private String apiKey;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalTime getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(LocalTime openingTime) {
        this.openingTime = openingTime;
    }

    public LocalTime getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(LocalTime closingTime) {
        this.closingTime = closingTime;
    }

    public LocalTime getLunchStart() {
        return lunchStart;
    }

    public void setLunchStart(LocalTime lunchStart) {
        this.lunchStart = lunchStart;
    }

    public LocalTime getLunchEnd() {
        return lunchEnd;
    }

    public void setLunchEnd(LocalTime lunchEnd) {
        this.lunchEnd = lunchEnd;
    }

    public int getSlotDurationMinutes() {
        return slotDurationMinutes;
    }

    public void setSlotDurationMinutes(int slotDurationMinutes) {
        this.slotDurationMinutes = slotDurationMinutes;
    }

    public int getMaxBookedPerDay() {
        return maxBookedPerDay;
    }

    public void setMaxBookedPerDay(int maxBookedPerDay) {
        this.maxBookedPerDay = maxBookedPerDay;
    }

    public int getCancellationWindowHours() {
        return cancellationWindowHours;
    }

    public void setCancellationWindowHours(int cancellationWindowHours) {
        this.cancellationWindowHours = cancellationWindowHours;
    }

    public List<LocalDate> getHolidays() {
        return holidays;
    }

    public void setHolidays(List<LocalDate> holidays) {
        this.holidays = holidays;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
