package ro.signsofter.caseobserver.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.signsofter.caseobserver.controller.dto.CreateCaseRequestDto;
import ro.signsofter.caseobserver.entity.CourtCase;
import ro.signsofter.caseobserver.exception.portal.PortalQueryException;
import ro.signsofter.caseobserver.external.dto.caseResponse.CaseDetailsDto;
import ro.signsofter.caseobserver.service.CourtCaseService;

@RestController
@RequestMapping("/api/cases")
public class CourtCaseController {
    private final CourtCaseService courtCaseService;

    public CourtCaseController(CourtCaseService courtCaseService) {
        this.courtCaseService = courtCaseService;
    }


    @PostMapping
    public ResponseEntity<?> createCase(@Valid @RequestBody CreateCaseRequestDto request) {
        try {
            CourtCase createdCase = courtCaseService.createCase(request);

            if  (null == request.getUser()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(createdCase);
            }

            courtCaseService.saveUserCase(request.getUser(), createdCase);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdCase);
        } catch (IllegalArgumentException | PortalQueryException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 1. Fetch case data from portal without saving
    @GetMapping("/fetch")
    public ResponseEntity<?> fetchCaseData(@RequestParam String caseNumber, @RequestParam String institution) {
        try {
            CaseDetailsDto caseDetails = courtCaseService.fetchCaseDetailsFromPortal(caseNumber, institution);
            return ResponseEntity.ok(caseDetails);
        } catch (PortalQueryException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. Refetch and update saved case data
    @PostMapping("/{id}/refetch")
    public ResponseEntity<?> refetchAndUpdateCase(@PathVariable Long id) {
        try {
            CourtCase updatedCase = courtCaseService.refetchAndUpdateCase(id);
            return ResponseEntity.ok(updatedCase);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        }
    }

    // 3. Activate monitoring on case with notification interval
    @PostMapping("/{id}/monitoring/activate")
    public ResponseEntity<?> activateMonitoring(@PathVariable Long id, @RequestParam int notificationIntervalMinutes) {
        try {
            CourtCase updatedCase = courtCaseService.activateMonitoring(id, notificationIntervalMinutes);
            return ResponseEntity.ok(updatedCase);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. Deactivate monitoring on case
    @PostMapping("/{id}/monitoring/deactivate")
    public ResponseEntity<?> deactivateMonitoring(@PathVariable Long id) {
        try {
            CourtCase updatedCase = courtCaseService.deactivateMonitoring(id);
            return ResponseEntity.ok(updatedCase);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 5. Update notification settings (interval, custom name)
    @PutMapping("/{id}/notification-settings")
    public ResponseEntity<?> updateNotificationSettings(@PathVariable Long id,
                                                        @RequestParam(required = false) Integer notificationIntervalMinutes,
                                                        @RequestParam(required = false) String customName) {
        try {
            CourtCase updatedCase = courtCaseService.updateNotificationSettings(id, notificationIntervalMinutes, customName);
            return ResponseEntity.ok(updatedCase);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
