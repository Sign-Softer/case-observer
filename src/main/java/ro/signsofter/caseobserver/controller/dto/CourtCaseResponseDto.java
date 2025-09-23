package ro.signsofter.caseobserver.controller.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CourtCaseResponseDto {
    private Long id;
    private String caseNumber;
    private String imposedName;
    private String department;
    private String proceduralStage;
    private String category;
    private String subject;
    private String courtName;
    private String status;
    private Boolean monitoringEnabled;
    private LocalDateTime lastUpdated;
    private List<HearingResponseDto> hearings;
    private List<PartyResponseDto> parties;
}


