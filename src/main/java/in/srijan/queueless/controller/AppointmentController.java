package in.srijan.queueless.controller;

import in.srijan.queueless.dto.request.BookAppointmentRequest;
import in.srijan.queueless.dto.request.CancelAppointmentRequest;
import in.srijan.queueless.dto.response.AppointmentResponse;
import in.srijan.queueless.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> book(
            @RequestHeader("x-user-id") Long patientId,
            @Valid @RequestBody BookAppointmentRequest request) {
        // x-user-id read directly for now - CurrentRequest/RoleInterceptor land in M9
        AppointmentResponse created = appointmentService.book(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancel(
            @PathVariable Long id,
            @RequestHeader("x-user-role") String callerRole,
            @RequestHeader("x-user-id") Long callerId,
            @RequestBody(required = false) CancelAppointmentRequest request) {
        CancelAppointmentRequest body = (request != null) ? request : new CancelAppointmentRequest();
        return ResponseEntity.ok(appointmentService.cancel(id, callerRole, callerId, body));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponse> complete(
            @PathVariable Long id,
            @RequestHeader("x-user-role") String callerRole,
            @RequestHeader("x-user-id") Long callerId) {
        return ResponseEntity.ok(appointmentService.complete(id, callerRole, callerId));
    }

    @PatchMapping("/{id}/no-show")
    public ResponseEntity<AppointmentResponse> noShow(
            @PathVariable Long id,
            @RequestHeader("x-user-role") String callerRole,
            @RequestHeader("x-user-id") Long callerId) {
        return ResponseEntity.ok(appointmentService.noShow(id, callerRole, callerId));
    }
}
