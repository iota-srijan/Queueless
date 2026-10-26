package in.srijan.queueless.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class UpdateDoctorRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "specialization is required")
    private String specialization;

    @NotBlank(message = "phone is required")
    @Pattern(regexp = "\\d{10}", message = "phone must be exactly 10 digits")
    private String phone;

    @NotNull(message = "Consultation Fee is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Consultation Fee must be positive")
    private BigDecimal consultationFee;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }
}
