package ro.signsofter.caseobserver.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCaseRequestDto {
    @NotBlank(message = "Case number is required")
    private String caseNumber;
    @NotBlank(message = "Case institution is required")
    private String institution;
    private String caseName;
    private String user;
}