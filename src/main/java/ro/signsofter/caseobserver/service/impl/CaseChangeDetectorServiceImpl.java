package ro.signsofter.caseobserver.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.signsofter.caseobserver.entity.CourtCase;
import ro.signsofter.caseobserver.entity.Hearing;
import ro.signsofter.caseobserver.entity.Party;
import ro.signsofter.caseobserver.external.dto.caseResponse.CaseDetailsDto;
import ro.signsofter.caseobserver.external.dto.caseResponse.HearingDto;
import ro.signsofter.caseobserver.external.dto.caseResponse.PartyDto;
import ro.signsofter.caseobserver.service.CaseChangeDetectorService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CaseChangeDetectorServiceImpl implements CaseChangeDetectorService {

    @Override
    @Transactional(readOnly = true)
    public CaseChanges detectChanges(CourtCase existingCase, CaseDetailsDto newData) {
        CaseChanges changes = new CaseChanges();
        
        // Check basic field changes
        detectBasicFieldChanges(existingCase, newData, changes);
        
        // Check hearing changes
        detectHearingChanges(existingCase, newData, changes);
        
        // Check party changes
        detectPartyChanges(existingCase, newData, changes);
        
        changes.setHasChanges(changes.hasAnyChanges());
        return changes;
    }
    
    private void detectBasicFieldChanges(CourtCase existingCase, CaseDetailsDto newData, CaseChanges changes) {
        // Status changes
        if (!equalsIgnoreCase(existingCase.getStatus(), newData.getProceduralStage())) {
            changes.setStatusChanged(true);
            changes.setOldStatus(existingCase.getStatus());
            changes.setNewStatus(newData.getProceduralStage());
        }
        
        // Procedural stage changes
        if (!equalsIgnoreCase(existingCase.getProceduralStage(), newData.getProceduralStage())) {
            changes.setProceduralStageChanged(true);
            changes.setOldProceduralStage(existingCase.getProceduralStage());
            changes.setNewProceduralStage(newData.getProceduralStage());
        }
        
        // Category changes
        if (!equalsIgnoreCase(existingCase.getCategory(), newData.getCaseCategory())) {
            changes.setCategoryChanged(true);
            changes.setOldCategory(existingCase.getCategory());
            changes.setNewCategory(newData.getCaseCategory());
        }
        
        // Subject changes
        if (!equalsIgnoreCase(existingCase.getSubject(), newData.getSubject())) {
            changes.setSubjectChanged(true);
            changes.setOldSubject(existingCase.getSubject());
            changes.setNewSubject(newData.getSubject());
        }
        
        // Department changes
        if (!equalsIgnoreCase(existingCase.getDepartment(), newData.getDepartment())) {
            changes.setDepartmentChanged(true);
            changes.setOldDepartment(existingCase.getDepartment());
            changes.setNewDepartment(newData.getDepartment());
        }
    }
    
    private void detectHearingChanges(CourtCase existingCase, CaseDetailsDto newData, CaseChanges changes) {
        List<Hearing> existingHearings = existingCase.getHearings();
        List<HearingDto> newHearings = newData.getHearings();
        
        if (existingHearings == null) existingHearings = new ArrayList<>();
        if (newHearings == null) newHearings = new ArrayList<>();
        
        // Create maps for easier comparison
        Map<String, Hearing> existingHearingMap = existingHearings.stream()
                .collect(Collectors.toMap(
                    h -> generateHearingKey(h),
                    h -> h,
                    (existing, replacement) -> existing
                ));
        
        Map<String, HearingDto> newHearingMap = newHearings.stream()
                .collect(Collectors.toMap(
                    h -> generateHearingKey(h),
                    h -> h,
                    (existing, replacement) -> existing
                ));
        
        // Find added hearings
        for (HearingDto newHearing : newHearings) {
            String key = generateHearingKey(newHearing);
            if (!existingHearingMap.containsKey(key)) {
                CaseChanges.HearingChange change = new CaseChanges.HearingChange();
                change.setType("ADDED");
                change.setHearing(newHearing);
                change.setDescription("New hearing scheduled: " + formatHearingDate(newHearing));
                changes.getHearingChanges().add(change);
                changes.setHearingsChanged(true);
            }
        }
        
        // Find removed hearings
        for (Hearing existingHearing : existingHearings) {
            String key = generateHearingKey(existingHearing);
            if (!newHearingMap.containsKey(key)) {
                CaseChanges.HearingChange change = new CaseChanges.HearingChange();
                change.setType("REMOVED");
                change.setDescription("Hearing removed: " + formatHearingDate(existingHearing));
                changes.getHearingChanges().add(change);
                changes.setHearingsChanged(true);
            }
        }
        
        // Find updated hearings
        for (HearingDto newHearing : newHearings) {
            String key = generateHearingKey(newHearing);
            Hearing existingHearing = existingHearingMap.get(key);
            if (existingHearing != null && !isHearingEqual(existingHearing, newHearing)) {
                CaseChanges.HearingChange change = new CaseChanges.HearingChange();
                change.setType("UPDATED");
                change.setHearing(newHearing);
                change.setDescription("Hearing updated: " + formatHearingDate(newHearing));
                changes.getHearingChanges().add(change);
                changes.setHearingsChanged(true);
            }
        }
    }
    
    private void detectPartyChanges(CourtCase existingCase, CaseDetailsDto newData, CaseChanges changes) {
        List<Party> existingParties = existingCase.getParties();
        List<PartyDto> newParties = newData.getParties();
        
        if (existingParties == null) existingParties = new ArrayList<>();
        if (newParties == null) newParties = new ArrayList<>();
        
        // Create maps for easier comparison
        Map<String, Party> existingPartyMap = existingParties.stream()
                .collect(Collectors.toMap(
                    p -> generatePartyKey(p),
                    p -> p,
                    (existing, replacement) -> existing
                ));
        
        Map<String, PartyDto> newPartyMap = newParties.stream()
                .collect(Collectors.toMap(
                    p -> generatePartyKey(p),
                    p -> p,
                    (existing, replacement) -> existing
                ));
        
        // Find added parties
        for (PartyDto newParty : newParties) {
            String key = generatePartyKey(newParty);
            if (!existingPartyMap.containsKey(key)) {
                CaseChanges.PartyChange change = new CaseChanges.PartyChange();
                change.setType("ADDED");
                change.setParty(newParty);
                change.setDescription("New party added: " + newParty.getName() + " (" + newParty.getRole() + ")");
                changes.getPartyChanges().add(change);
                changes.setPartiesChanged(true);
            }
        }
        
        // Find removed parties
        for (Party existingParty : existingParties) {
            String key = generatePartyKey(existingParty);
            if (!newPartyMap.containsKey(key)) {
                CaseChanges.PartyChange change = new CaseChanges.PartyChange();
                change.setType("REMOVED");
                change.setDescription("Party removed: " + existingParty.getName() + " (" + existingParty.getRole() + ")");
                changes.getPartyChanges().add(change);
                changes.setPartiesChanged(true);
            }
        }
        
        // Find updated parties
        for (PartyDto newParty : newParties) {
            String key = generatePartyKey(newParty);
            Party existingParty = existingPartyMap.get(key);
            if (existingParty != null && !isPartyEqual(existingParty, newParty)) {
                CaseChanges.PartyChange change = new CaseChanges.PartyChange();
                change.setType("UPDATED");
                change.setParty(newParty);
                change.setDescription("Party updated: " + newParty.getName() + " (" + newParty.getRole() + ")");
                changes.getPartyChanges().add(change);
                changes.setPartiesChanged(true);
            }
        }
    }
    
    private String generateHearingKey(Hearing hearing) {
        return hearing.getHearingDate() != null ? 
            hearing.getHearingDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : 
            "unknown_" + System.currentTimeMillis();
    }
    
    private String generateHearingKey(HearingDto hearing) {
        // Generate key that matches Hearing entity key format
        try {
            LocalDateTime hearingDateTime = parseDate(hearing.getDate(), hearing.getTime());
            return hearingDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            return hearing.getDate() + "_" + hearing.getTime();
        }
    }
    
    private String generatePartyKey(Party party) {
        return party.getName() + "_" + party.getRole();
    }
    
    private String generatePartyKey(PartyDto party) {
        return party.getName() + "_" + party.getRole();
    }
    
    private boolean isHearingEqual(Hearing existing, HearingDto newHearing) {
        return equalsIgnoreCase(existing.getSolution(), newHearing.getSolution()) &&
               equalsIgnoreCase(existing.getDescription(), newHearing.getSummary()) &&
               equalsIgnoreCase(existing.getJudicialPanel(), newHearing.getJudicialPanel());
    }
    
    private boolean isPartyEqual(Party existing, PartyDto newParty) {
        return equalsIgnoreCase(existing.getName(), newParty.getName()) &&
               equalsIgnoreCase(existing.getRole(), newParty.getRole());
    }
    
    private String formatHearingDate(HearingDto hearing) {
        return hearing.getDate() + " " + hearing.getTime();
    }
    
    private String formatHearingDate(Hearing hearing) {
        return hearing.getHearingDate() != null ? 
            hearing.getHearingDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : 
            "Unknown date";
    }
    
    private boolean equalsIgnoreCase(String str1, String str2) {
        if (str1 == null && str2 == null) return true;
        if (str1 == null || str2 == null) return false;
        return str1.equalsIgnoreCase(str2);
    }
    
    private LocalDateTime parseDate(String rawDate, String rawTime) {
        try {
            // If rawDate is a full ISO datetime
            if (rawDate.contains("T") && rawDate.length() > 10) {
                return LocalDateTime.parse(rawDate);
            }
            // If only date and time separated
            return LocalDateTime.parse(rawDate + "T" + rawTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        } catch (Exception e) {
            return LocalDateTime.now(); // fallback
        }
    }
}
