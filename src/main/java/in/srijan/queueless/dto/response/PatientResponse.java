package in.srijan.queueless.dto.response;

import in.srijan.queueless.entity.Patient;

import java.time.LocalDate;

public class PatientResponse {

    private final Long id;
    private final String name;
    private final String email;
    private final String phone;
    private final LocalDate dateOfBirth;

    public PatientResponse(Long id, String name, String email, String phone, LocalDate dateOfBirth) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
    }

    public static PatientResponse from(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getName(),
                patient.getEmail(),
                patient.getPhone(),
                patient.getDateOfBirth()
        );
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
}
