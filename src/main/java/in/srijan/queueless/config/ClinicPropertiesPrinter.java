package in.srijan.queueless.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class ClinicPropertiesPrinter implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ClinicPropertiesPrinter.class);

    private final ClinicProperties clinicProperties;

    public ClinicPropertiesPrinter(ClinicProperties clinicProperties) {
        this.clinicProperties = clinicProperties;
    }

    @Override
    public void run(String... args) {
        log.info("Loaded clinic config -> name={}, hours={}-{}, lunch={}-{}, slotMinutes={}, " +
                        "maxBookedPerDay={}, cancellationWindowHours={}, holidays={}",
                clinicProperties.getName(),
                clinicProperties.getOpeningTime(),
                clinicProperties.getClosingTime(),
                clinicProperties.getLunchStart(),
                clinicProperties.getLunchEnd(),
                clinicProperties.getSlotDurationMinutes(),
                clinicProperties.getMaxBookedPerDay(),
                clinicProperties.getCancellationWindowHours(),
                clinicProperties.getHolidays());
    }
}
