package in.srijan.queueless.service;

import in.srijan.queueless.config.ClinicProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Service
public class HolidayCalendar {

    private static final Logger log = LoggerFactory.getLogger(HolidayCalendar.class);

    private final ClinicProperties clinicProperties;
    private Set<LocalDate> holidays;

    public HolidayCalendar(ClinicProperties clinicProperties) {
        this.clinicProperties = clinicProperties;
    }

    @PostConstruct
    void loadHolidays() {
        holidays = new HashSet<>(clinicProperties.getHolidays());
        log.info("Loaded {} clinic holidays", holidays.size());
    }

    public boolean isHoliday(LocalDate date) {
        return holidays.contains(date);
    }
}
