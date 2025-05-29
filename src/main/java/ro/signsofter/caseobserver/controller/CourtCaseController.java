package ro.signsofter.caseobserver.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.signsofter.caseobserver.controller.dto.CreateCaseRequestDto;
import ro.signsofter.caseobserver.entity.CourtCase;
import ro.signsofter.caseobserver.exception.portal.PortalQueryException;
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
}
