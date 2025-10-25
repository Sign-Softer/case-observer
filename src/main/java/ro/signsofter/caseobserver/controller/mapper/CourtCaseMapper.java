package ro.signsofter.caseobserver.controller.mapper;

import ro.signsofter.caseobserver.controller.dto.CourtCaseResponseDto;
import ro.signsofter.caseobserver.controller.dto.HearingResponseDto;
import ro.signsofter.caseobserver.controller.dto.PartyResponseDto;
import ro.signsofter.caseobserver.entity.CourtCase;
import ro.signsofter.caseobserver.entity.Hearing;
import ro.signsofter.caseobserver.entity.Party;

import java.util.List;
import java.util.stream.Collectors;

public class CourtCaseMapper {

    public static CourtCaseResponseDto toDto(CourtCase courtCase) {
        CourtCaseResponseDto dto = new CourtCaseResponseDto();
        dto.setId(courtCase.getId());
        dto.setCaseNumber(courtCase.getCaseNumber());
        dto.setImposedName(courtCase.getImposedName());
        dto.setDepartment(courtCase.getDepartment());
        dto.setProceduralStage(courtCase.getProceduralStage());
        dto.setCategory(courtCase.getCategory());
        dto.setSubject(courtCase.getSubject());
        dto.setCourtName(courtCase.getCourtName());
        dto.setStatus(courtCase.getStatus());
        dto.setMonitoringEnabled(courtCase.getMonitoringEnabled());
        dto.setLastUpdated(courtCase.getLastUpdated());

        List<HearingResponseDto> hearings = courtCase.getHearings().stream()
                .map(CourtCaseMapper::toDto)
                .collect(Collectors.toList());
        dto.setHearings(hearings);

        List<PartyResponseDto> parties = courtCase.getParties().stream()
                .map(CourtCaseMapper::toDto)
                .collect(Collectors.toList());
        dto.setParties(parties);

        return dto;
    }

    public static HearingResponseDto toDto(Hearing h) {
        HearingResponseDto dto = new HearingResponseDto();
        dto.setId(h.getId());
        dto.setHearingDate(h.getHearingDate());
        dto.setPronouncementDate(h.getPronouncementDate());
        dto.setJudicialPanel(h.getJudicialPanel());
        dto.setSolution(h.getSolution());
        dto.setDescription(h.getDescription());
        return dto;
    }

    public static PartyResponseDto toDto(Party p) {
        PartyResponseDto dto = new PartyResponseDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setRole(p.getRole());
        return dto;
    }
}




