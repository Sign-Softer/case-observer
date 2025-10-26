package ro.signsofter.caseobserver.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "notification_settings")
public class NotificationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "case_id", nullable = false, unique = true)
    private CourtCase courtCase;

    @Column(name = "notification_interval_minutes", nullable = false)
    @Min(value = 1, message = "Notification interval must be at least 1 minute")
    private Integer notificationIntervalMinutes = 60; // Default 1 hour

    @Column(name = "email_enabled", nullable = false)
    private Boolean emailEnabled = true;

    @Column(name = "sms_enabled", nullable = false)
    private Boolean smsEnabled = false;

    @Column(name = "notify_on_hearing_changes", nullable = false)
    private Boolean notifyOnHearingChanges = true;

    @Column(name = "notify_on_status_changes", nullable = false)
    private Boolean notifyOnStatusChanges = true;

    @Column(name = "notify_on_party_changes", nullable = false)
    private Boolean notifyOnPartyChanges = true;

    @Column(name = "notify_on_procedural_stage_changes", nullable = false)
    private Boolean notifyOnProceduralStageChanges = true;

    @Column(name = "last_checked_at")
    private LocalDateTime lastCheckedAt;

    @Column(name = "next_check_at")
    private LocalDateTime nextCheckAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Helper method to calculate next check time
    public void calculateNextCheckTime() {
        if (lastCheckedAt != null) {
            this.nextCheckAt = lastCheckedAt.plusMinutes(notificationIntervalMinutes);
        } else {
            this.nextCheckAt = LocalDateTime.now().plusMinutes(notificationIntervalMinutes);
        }
    }

    // Helper method to check if it's time for next check
    public boolean isTimeForCheck() {
        return nextCheckAt == null || LocalDateTime.now().isAfter(nextCheckAt);
    }
}
