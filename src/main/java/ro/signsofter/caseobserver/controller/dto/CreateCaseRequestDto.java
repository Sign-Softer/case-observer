package ro.signsofter.caseobserver.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCaseRequestDto {
    @NotBlank(message = "Case ID is required")
    private String caseId;

    private String caseName;
    private String courtName;
    private String status;
}