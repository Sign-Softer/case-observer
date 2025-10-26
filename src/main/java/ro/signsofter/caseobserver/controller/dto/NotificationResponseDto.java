package ro.signsofter.caseobserver.controller.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponseDto {
    private Long id;
    private String message;
    private LocalDateTime sentAt;
    private String caseNumber;
    private Long caseId;
}
