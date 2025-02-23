package ro.signsofter.caseobserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "court_case")
public class CourtCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false, unique = true)
    private String  caseId;

    @Column(name = "case_name")
    private String caseName;

    @Column(name = "court_name")
    private String courtName;

    @Column(name = "status")
    private String status;

    @UpdateTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "monitoring_enabled", nullable = false)
    private Boolean monitoringEnabled = true;
}
