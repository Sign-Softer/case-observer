package ro.signsofter.caseobserver.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "court_case")
public class CourtCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "number", nullable = false, unique = true)
    private String caseNumber;

    @Column(name = "imposed_name")
    private String imposedName;

    // use enum
    @Column(name = "department")
    private String department;

    // use enum
    @Column(name = "procedural_stage")
    private String proceduralStage;

    // TODO use enum
    @Column(name = "category")
    private String category;

    // TODO use enum
    @Column(name = "subject")
    private String subject;

    // use enum
    @Column(name = "court_name")
    private String courtName;

    @Column(name = "status")
    private String status;

    @UpdateTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "monitoring_enabled", nullable = false)
    private Boolean monitoringEnabled = true;

    @OneToMany(mappedBy = "courtCase", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Hearing> hearings = new ArrayList<>();
}
