package ro.signsofter.caseobserver.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.signsofter.caseobserver.controller.dto.CreateCaseRequestDto;
import ro.signsofter.caseobserver.entity.*;
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

        List<Party> parties = externalData.getParties().stream()
                .map(p -> {
                    Party party = new Party();
                    party.setCourtCase(courtCase);
                    party.setName(p.getName());
                    party.setRole(p.getRole());

                    return party;
                })
                .collect(Collectors.toList());

        courtCase.setHearings(hearings);
        courtCase.setParties(parties);

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

    public CaseDetailsDto fetchCaseDetailsFromPortal(String caseNumber, String institution) throws PortalQueryException {
        return portalQueryService.fetchCaseDetails(caseNumber, institution);
    }

    // Refetch data from portal and update saved case
    public CourtCase refetchAndUpdateCase(Long id) throws PortalQueryException {
        CourtCase existingCase = courtCaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Case not found with id " + id));

        CaseDetailsDto externalData = portalQueryService.fetchCaseDetails(existingCase.getCaseNumber(), existingCase.getCourtName());

        if (externalData == null || externalData.getNumber() == null) {
            throw new IllegalArgumentException("Failed to fetch case data from portal");
        }

        // Update fields (similar to createCase)
        existingCase.setStatus(externalData.getProceduralStage());
        existingCase.setCategory(externalData.getCaseCategory());
        existingCase.setDepartment(externalData.getDepartment());
        existingCase.setSubject(externalData.getSubject());
        existingCase.setProceduralStage(externalData.getProceduralStage());

        // TODO: Update hearings and parties if needed

        return courtCaseRepository.save(existingCase);
    }

    // Activate monitoring with notification interval
    public CourtCase activateMonitoring(Long id, int notificationIntervalMinutes) {
        if (notificationIntervalMinutes <= 0) {
            throw new IllegalArgumentException("Notification interval must be positive");
        }

        CourtCase courtCase = courtCaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Case not found with id " + id));

        courtCase.setMonitoringEnabled(true);

        // TODO: Save notification interval in notification settings entity or related config

        return courtCaseRepository.save(courtCase);
    }

    // Deactivate monitoring
    public CourtCase deactivateMonitoring(Long id) {
        CourtCase courtCase = courtCaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Case not found with id " + id));

        courtCase.setMonitoringEnabled(false);

        return courtCaseRepository.save(courtCase);
    }

    // Update notification settings (interval, custom name)
    public CourtCase updateNotificationSettings(Long id, Integer notificationIntervalMinutes, String customName) {
        CourtCase courtCase = courtCaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Case not found with id " + id));

        // TODO: Update notification interval and custom name in notification settings entity or related config
        // For now, just a placeholder

        // Example: if you have a NotificationSettings entity linked to CourtCase, update it here

        return courtCaseRepository.save(courtCase);
    }
}
