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
}

