package ro.signsofter.caseobserver.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_case")
public class UserCase {
    @EmbeddedId
    private UserCaseId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @MapsId("caseId")
    @JoinColumn(name = "case_id", nullable = false)
    private CourtCase courtCase;

    @Column(name = "custom_title")
    private String customTitle;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "monitoring_started_at")
    private LocalDateTime monitoringStartedAt;

    @Embeddable
    @Data
    public static class UserCaseId implements Serializable {
        private Long userId;
        private Long caseId;
    }
}


