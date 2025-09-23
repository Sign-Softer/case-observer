package ro.signsofter.caseobserver.controller.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HearingResponseDto {
    private Long id;
    private LocalDateTime hearingDate;
    private LocalDateTime pronouncementDate;
    private String judicialPanel;
    private String solution;
    private String description;
}


