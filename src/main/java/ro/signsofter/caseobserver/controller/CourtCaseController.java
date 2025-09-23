package ro.signsofter.caseobserver.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.signsofter.caseobserver.controller.dto.CourtCaseResponseDto;
import ro.signsofter.caseobserver.controller.dto.CreateCaseRequestDto;
import ro.signsofter.caseobserver.controller.mapper.CourtCaseMapper;
import ro.signsofter.caseobserver.entity.CourtCase;
import ro.signsofter.caseobserver.exception.portal.PortalQueryException;
import ro.signsofter.caseobserver.external.dto.caseResponse.CaseDetailsDto;
import ro.signsofter.caseobserver.service.CourtCaseService;

import java.util.Optional;

@RestController
@RequestMapping("/api/cases")
public class CourtCaseController {
    private final CourtCaseService courtCaseService;

    public CourtCaseController(CourtCaseService courtCaseService) {
        this.courtCaseService = courtCaseService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<CourtCaseResponseDto> getCase(@PathVariable Long id) {
        Optional<CourtCase> caseOpt = courtCaseService.getCaseById(id);
        return caseOpt.map(c -> ResponseEntity.ok(CourtCaseMapper.toDto(c)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    

    @PostMapping
    public ResponseEntity<CourtCaseResponseDto> createCase(@Valid @RequestBody CreateCaseRequestDto request) throws PortalQueryException {
        CourtCase createdCase = courtCaseService.createCase(request);
        if (request.getUser() != null) {
            courtCaseService.saveUserCase(request.getUser(), createdCase);
        }
        return ResponseEntity.ok(CourtCaseMapper.toDto(createdCase));
    }

    // 1. Fetch case data from portal without saving
    @GetMapping("/fetch")
    public ResponseEntity<CaseDetailsDto> fetchCaseData(@RequestParam String caseNumber, @RequestParam String institution) throws PortalQueryException {
        CaseDetailsDto caseDetails = courtCaseService.fetchCaseDetailsFromPortal(caseNumber, institution);
        return ResponseEntity.ok(caseDetails);
    }

    // 2. Refetch and update saved case data
    @PostMapping("/{id}/refetch")
    public ResponseEntity<CourtCaseResponseDto> refetchAndUpdateCase(@PathVariable Long id) throws PortalQueryException {
        CourtCase updatedCase = courtCaseService.refetchAndUpdateCase(id);
        return ResponseEntity.ok(CourtCaseMapper.toDto(updatedCase));
    }

    // 3. Activate monitoring on case with notification interval
    @PostMapping("/{id}/monitoring/activate")
    public ResponseEntity<CourtCaseResponseDto> activateMonitoring(@PathVariable Long id, @RequestParam int notificationIntervalMinutes) {
        CourtCase updatedCase = courtCaseService.activateMonitoring(id, notificationIntervalMinutes);
        return ResponseEntity.ok(CourtCaseMapper.toDto(updatedCase));
    }

    // 4. Deactivate monitoring on case
    @PostMapping("/{id}/monitoring/deactivate")
    public ResponseEntity<CourtCaseResponseDto> deactivateMonitoring(@PathVariable Long id) {
        CourtCase updatedCase = courtCaseService.deactivateMonitoring(id);
        return ResponseEntity.ok(CourtCaseMapper.toDto(updatedCase));
    }

    // 5. Update notification settings (interval, custom name)
    @PutMapping("/{id}/notification-settings")
    public ResponseEntity<CourtCaseResponseDto> updateNotificationSettings(@PathVariable Long id,
                                                        @RequestParam(required = false) Integer notificationIntervalMinutes,
                                                        @RequestParam(required = false) String customName) {
        CourtCase updatedCase = courtCaseService.updateNotificationSettings(id, notificationIntervalMinutes, customName);
        return ResponseEntity.ok(CourtCaseMapper.toDto(updatedCase));
    }
}
