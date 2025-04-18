package ro.signsofter.caseobserver.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.signsofter.caseobserver.controller.dto.CreateCaseRequestDto;
import ro.signsofter.caseobserver.entity.CourtCase;
import ro.signsofter.caseobserver.repository.CourtCaseRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CourtCaseService {

    @Autowired
    private CourtCaseRepository courtCaseRepository;

    public List<CourtCase> getAllCases() {
        return courtCaseRepository.findAll();
    }

    public Optional<CourtCase> getCaseById(Long id) {
        return courtCaseRepository.findById(id);
    }

    public CourtCase createCase(CreateCaseRequestDto request) {
        // Check if case already exists
        if (courtCaseRepository.existsByCaseId(request.getCaseId())) {
            throw new IllegalArgumentException("Case with ID " + request.getCaseId() + " already exists");
        }

        // Map DTO to entity
        CourtCase courtCase = new CourtCase();
        courtCase.setCaseId(request.getCaseId());
        courtCase.setCaseName(request.getCaseName());
        courtCase.setCourtName(request.getCourtName());
        courtCase.setStatus(request.getStatus());

        return courtCaseRepository.save(courtCase);
    }

    public CourtCase updateCase(Long id, CourtCase caseDetails) {
        CourtCase courtCase = courtCaseRepository.findById(id).orElseThrow(() -> new RuntimeException("Case not found"));
        courtCase.setCaseId(caseDetails.getCaseId());
        courtCase.setCaseName(caseDetails.getCaseName());
        courtCase.setCourtName(caseDetails.getCourtName());
        courtCase.setStatus(caseDetails.getStatus());
        return courtCaseRepository.save(courtCase);
    }

    public void deleteCase(Long id) {
        courtCaseRepository.deleteById(id);
    }
}
