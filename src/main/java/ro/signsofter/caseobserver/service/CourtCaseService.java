package ro.signsofter.caseobserver.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.signsofter.caseobserver.controller.dto.CreateCaseRequestDto;
import ro.signsofter.caseobserver.entity.CourtCase;
import ro.signsofter.caseobserver.entity.Hearing;
import ro.signsofter.caseobserver.entity.User;
import ro.signsofter.caseobserver.entity.UserCase;
import ro.signsofter.caseobserver.exception.portal.PortalQueryException;
import ro.signsofter.caseobserver.external.PortalQueryService;
import ro.signsofter.caseobserver.external.dto.caseResponse.CaseDetailsDto;
import ro.signsofter.caseobserver.repository.CourtCaseRepository;
import ro.signsofter.caseobserver.repository.UserCaseRepository;
import ro.signsofter.caseobserver.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CourtCaseService {

    @Autowired
    private CourtCaseRepository courtCaseRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserCaseRepository userCaseRepository;
    @Autowired
    private PortalQueryService portalQueryService;

    public List<CourtCase> getAllCases() {
        return courtCaseRepository.findAll();
    }

    public Optional<CourtCase> getCaseById(Long id) {
        return courtCaseRepository.findById(id);
    }

    public CourtCase createCase(CreateCaseRequestDto request) throws PortalQueryException {
        // 1. Avoid duplicates
        if (courtCaseRepository.existsByCaseNumber(request.getCaseNumber())) {
            throw new IllegalArgumentException("Case with ID " + request.getCaseNumber() + " already exists");
        }

        // 2. Fetch from SOAP
        CaseDetailsDto externalData = portalQueryService.fetchCaseDetails(request.getCaseNumber(), request.getInstitution());

        if (externalData == null || externalData.getNumber() == null) {
            throw new IllegalArgumentException("Case could not be fetched from portal or response is incomplete.");
        }

        CourtCase courtCase = new CourtCase();
        // TODO handle enums
        courtCase.setCaseNumber(externalData.getNumber());
        courtCase.setImposedName(request.getCaseName());
        courtCase.setCourtName(externalData.getInstitution());
        courtCase.setStatus(externalData.getProceduralStage());
        courtCase.setCategory(externalData.getCaseCategory());
        courtCase.setDepartment(externalData.getDepartment());
        courtCase.setSubject(externalData.getSubject());
        courtCase.setProceduralStage(externalData.getProceduralStage());

        // Must be a parameter
        courtCase.setMonitoringEnabled(true);

        List<Hearing> hearings = externalData.getHearings().stream()
                .map(h -> {
                    Hearing hearing = new Hearing();
                    hearing.setCourtCase(courtCase);
                    hearing.setHearingDate(parseDate(h.getDate(), h.getTime()));
                    hearing.setSolution(h.getSolution());
                    hearing.setDescription(h.getSummary());
                    hearing.setJudicialPanel(h.getJudicialPanel());
                    hearing.setPronouncementDate(parseDate(h.getDate(), h.getTime()));

                    return hearing;
                })
                .collect(Collectors.toList());

        courtCase.setHearings(hearings);

        return courtCaseRepository.save(courtCase);
    }

    public void saveUserCase(String username, CourtCase courtCase) {
        User user = this.userRepository.findByUsername(username).orElseThrow();
        System.out.println("EMMAIL " + user.getEmail());
        UserCase userCase = new UserCase();
        userCase.setUser(user);
        userCase.setCourtCase(courtCase);

        userCaseRepository.save(userCase);
    }

    public CourtCase updateCase(Long id, CourtCase caseDetails) {
        CourtCase courtCase = courtCaseRepository.findById(id).orElseThrow(() -> new RuntimeException("Case not found"));
        courtCase.setCaseNumber(caseDetails.getCaseNumber());
        courtCase.setImposedName(caseDetails.getImposedName());
        courtCase.setCourtName(caseDetails.getCourtName());
        courtCase.setStatus(caseDetails.getStatus());
        return courtCaseRepository.save(courtCase);
    }

    public void deleteCase(Long id) {
        courtCaseRepository.deleteById(id);
    }

    private LocalDateTime parseDate(String rawDate, String rawTime) {
        try {
            // If rawDate is a full ISO datetime
            if (rawDate.contains("T") && rawDate.length() > 10) {
                return LocalDateTime.parse(rawDate);
            }

            // If only date and time separated
            return LocalDateTime.parse(rawDate + "T" + rawTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Failed to parse date/time: " + rawDate + " " + rawTime, e);
        }
    }
}
