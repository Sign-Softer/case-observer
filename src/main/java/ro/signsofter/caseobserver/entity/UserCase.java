package ro.signsofter.caseobserver.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

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

    @Embeddable
    @Data
    public static class UserCaseId implements Serializable {
        private Long userId;
        private Long caseId;
    }
}


