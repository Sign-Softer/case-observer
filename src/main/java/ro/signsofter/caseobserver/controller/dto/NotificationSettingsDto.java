package ro.signsofter.caseobserver.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationSettingsDto {
    
    @NotNull(message = "Notification interval is required")
    @Min(value = 1, message = "Notification interval must be at least 1 minute")
    private Integer notificationIntervalMinutes = 60;
    
    @NotNull(message = "Email enabled setting is required")
    private Boolean emailEnabled = true;
    
    @NotNull(message = "SMS enabled setting is required")
    private Boolean smsEnabled = false;
    
    @NotNull(message = "Hearing changes notification setting is required")
    private Boolean notifyOnHearingChanges = true;
    
    @NotNull(message = "Status changes notification setting is required")
    private Boolean notifyOnStatusChanges = true;
    
    @NotNull(message = "Party changes notification setting is required")
    private Boolean notifyOnPartyChanges = true;
    
    @NotNull(message = "Procedural stage changes notification setting is required")
    private Boolean notifyOnProceduralStageChanges = true;
}
