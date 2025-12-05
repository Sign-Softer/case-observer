package ro.signsofter.caseobserver.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserProfileDto {
    private Long id;

    @NotBlank
    private String username;

    @Email
    @NotBlank
    private String email;

    private String role; // Role is needed for frontend context (e.g., UI permissions), but not displayed in profile settings UI
}

