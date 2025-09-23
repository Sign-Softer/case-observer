package ro.signsofter.caseobserver.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDto {
    private Long id;

    @NotBlank
    private String username;

    @Email
    @NotBlank
    private String email;

    // Note: For real apps, handle password separately
    private String password;

    private String role;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


