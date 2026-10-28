package in.srijan.queueless.controller;

import in.srijan.queueless.dto.request.GenerateSlotsRequest;
import in.srijan.queueless.dto.response.SlotResponse;
import in.srijan.queueless.enums.SlotStatus;
import in.srijan.queueless.service.SlotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
public class SlotController {

    private final SlotService slotService;

    public SlotController(SlotService slotService) {
        this.slotService = slotService;
    }

    @PostMapping("/api/doctors/{doctorId}/slots/generate")
    public ResponseEntity<List<SlotResponse>> generate(
            @PathVariable Long doctorId,
            @Valid @RequestBody GenerateSlotsRequest request) {
        List<SlotResponse> created = slotService.generate(doctorId, request.getDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/api/doctors/{doctorId}/slots")
    public ResponseEntity<List<SlotResponse>> list(
            @PathVariable Long doctorId,
            @RequestParam LocalDate date,
            @RequestParam(required = false) SlotStatus status) {
        return ResponseEntity.ok(slotService.list(doctorId, date, status));
    }

    @PatchMapping("/api/slots/{id}/block")
    public ResponseEntity<SlotResponse> block(@PathVariable Long id) {
        return ResponseEntity.ok(slotService.block(id));
    }
}
