package in.srijan.queueless.seed;

import in.srijan.queueless.entity.Doctor;
import in.srijan.queueless.entity.Patient;
import in.srijan.queueless.repository.DoctorRepository;
import in.srijan.queueless.repository.PatientRepository;
import in.srijan.queueless.service.SlotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final SlotService slotService;

    public DevDataSeeder(DoctorRepository doctorRepository, PatientRepository patientRepository,
                          SlotService slotService) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.slotService = slotService;
    }

    @Override
    public void run(String... args) {
        if (doctorRepository.count() > 0) {
            log.info("Dev data already present, skipping seed");
            return;
        }

        Doctor mehra = saveDoctor("Dr. Mehra", "Dermatology", "mehra@queueless.dev",
                "9876543210", "500");
        Doctor rao = saveDoctor("Dr. Rao", "Cardiology", "rao@queueless.dev",
                "9876543211", "700");
        Doctor iyer = saveDoctor("Dr. Iyer", "Pediatrics", "iyer@queueless.dev",
                "9876543212", "450");

        savePatient("Asha Kumar", "asha@queueless.dev", "9123456780", "2000-05-12");
        savePatient("Rahul Singh", "rahul@queueless.dev", "9123456781", "1995-03-20");

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        for (Doctor doctor : new Doctor[]{mehra, rao, iyer}) {
            generateTomorrowSlots(doctor, tomorrow);
        }

        log.info("Dev data seeded: 3 doctors, 2 patients, slots generated for {}", tomorrow);
    }

    private Doctor saveDoctor(String name, String specialization, String email, String phone,
                               String fee) {
        Doctor doctor = new Doctor();
        doctor.setName(name);
        doctor.setSpecialization(specialization);
        doctor.setEmail(email);
        doctor.setPhone(phone);
        doctor.setConsultationFee(new BigDecimal(fee));
        return doctorRepository.save(doctor);
    }

    private void savePatient(String name, String email, String phone, String dob) {
        Patient patient = new Patient();
        patient.setName(name);
        patient.setEmail(email);
        patient.setPhone(phone);
        patient.setDateOfBirth(LocalDate.parse(dob));
        patientRepository.save(patient);
    }

    private void generateTomorrowSlots(Doctor doctor, LocalDate date) {
        try {
            slotService.generate(doctor.getId(), date);
        } catch (Exception e) {
            // don't let a seeding hiccup (e.g. tomorrow happens to be a configured
            // holiday) prevent the whole app from starting
            log.warn("Could not seed slots for {} on {}: {}", doctor.getName(), date, e.getMessage());
        }
    }
}
