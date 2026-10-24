package in.srijan.queueless.dto.response;

import in.srijan.queueless.entity.Doctor;

import java.math.BigDecimal;

public class DoctorResponse {

    private final Long id;
    private final String name;
    private final String specialization;
    private final String email;
    private final String phone;
    private final BigDecimal consultationFee;
    private final boolean active;

    public DoctorResponse(Long id, String name, String specialization, String email,
                           String phone, BigDecimal consultationFee, boolean active) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.email = email;
        this.phone = phone;
        this.consultationFee = consultationFee;
        this.active = active;
    }

    public static DoctorResponse from(Doctor doctor) {
        return new DoctorResponse(
                doctor.getId(),
                doctor.getName(),
                doctor.getSpecialization(),
                doctor.getEmail(),
                doctor.getPhone(),
                doctor.getConsultationFee(),
                doctor.isActive()
        );
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public boolean isActive() {
        return active;
    }
}
