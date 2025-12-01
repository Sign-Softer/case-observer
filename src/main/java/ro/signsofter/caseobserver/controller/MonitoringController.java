package ro.signsofter.caseobserver.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ro.signsofter.caseobserver.controller.dto.ApiResponse;
import ro.signsofter.caseobserver.controller.dto.NotificationSettingsDto;
import ro.signsofter.caseobserver.controller.dto.NotificationResponseDto;
import ro.signsofter.caseobserver.entity.Notification;
import ro.signsofter.caseobserver.entity.NotificationSettings;
import ro.signsofter.caseobserver.service.CaseMonitoringService;
import ro.signsofter.caseobserver.service.NotificationService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final CaseMonitoringService caseMonitoringService;
    private final NotificationService notificationService;

    public MonitoringController(CaseMonitoringService caseMonitoringService, NotificationService notificationService) {
        this.caseMonitoringService = caseMonitoringService;
        this.notificationService = notificationService;
    }

    @PostMapping("/cases/{caseId}/start")
    public ResponseEntity<ApiResponse<String>> startMonitoring(
            @PathVariable Long caseId,
            @RequestParam(defaultValue = "60") Integer intervalMinutes) {
        try {
            caseMonitoringService.startMonitoringCase(caseId, intervalMinutes);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Monitoring started successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }

    @PostMapping("/cases/{caseId}/stop")
    public ResponseEntity<ApiResponse<String>> stopMonitoring(@PathVariable Long caseId) {
        try {
            caseMonitoringService.stopMonitoringCase(caseId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Monitoring stopped successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }

    @GetMapping("/cases/{caseId}/status")
    public ResponseEntity<ApiResponse<Boolean>> getMonitoringStatus(@PathVariable Long caseId) {
        try {
            boolean isMonitored = caseMonitoringService.isCaseBeingMonitored(caseId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Monitoring status retrieved", isMonitored));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }

    @PutMapping("/cases/{caseId}/settings")
    public ResponseEntity<ApiResponse<String>> updateNotificationSettings(
            @PathVariable Long caseId,
            @Valid @RequestBody NotificationSettingsDto settingsDto) {
        try {
            NotificationSettings settings = new NotificationSettings();
            settings.setNotificationIntervalMinutes(settingsDto.getNotificationIntervalMinutes());
            settings.setEmailEnabled(settingsDto.getEmailEnabled());
            settings.setSmsEnabled(settingsDto.getSmsEnabled());
            settings.setNotifyOnHearingChanges(settingsDto.getNotifyOnHearingChanges());
            settings.setNotifyOnStatusChanges(settingsDto.getNotifyOnStatusChanges());
            settings.setNotifyOnPartyChanges(settingsDto.getNotifyOnPartyChanges());
            settings.setNotifyOnProceduralStageChanges(settingsDto.getNotifyOnProceduralStageChanges());

            caseMonitoringService.updateNotificationSettings(caseId, settings);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Notification settings updated successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<NotificationResponseDto>>> getUserNotifications() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            // Get user ID from username (you might need to implement this)
            // For now, we'll use a placeholder approach
            List<Notification> notifications = notificationService.getNotificationsForUser(3L); // TODO: Get actual user ID
            
            List<NotificationResponseDto> notificationDtos = notifications.stream()
                    .map(this::mapToNotificationDto)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Notifications retrieved successfully", notificationDtos));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }

    @GetMapping("/notifications/cases/{caseId}")
    public ResponseEntity<ApiResponse<List<NotificationResponseDto>>> getCaseNotifications(@PathVariable Long caseId) {
        try {
            List<Notification> notifications = notificationService.getNotificationsForCase(caseId);
            
            List<NotificationResponseDto> notificationDtos = notifications.stream()
                    .map(this::mapToNotificationDto)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Case notifications retrieved successfully", notificationDtos));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }

    @PostMapping("/notifications/{notificationId}/read")
    public ResponseEntity<ApiResponse<String>> markNotificationAsRead(@PathVariable Long notificationId) {
        try {
            notificationService.markNotificationAsRead(notificationId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Notification marked as read", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }

    @PostMapping("/cases/{caseId}/check-now")
    public ResponseEntity<ApiResponse<String>> checkCaseNow(@PathVariable Long caseId) {
        try {
            caseMonitoringService.checkCaseForUpdates(caseId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Case checked for updates", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }

    private NotificationResponseDto mapToNotificationDto(Notification notification) {
        NotificationResponseDto dto = new NotificationResponseDto();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setSentAt(notification.getSentAt());
        dto.setCaseNumber(notification.getCourtCase().getCaseNumber());
        dto.setCaseId(notification.getCourtCase().getId());
        return dto;
    }
}
