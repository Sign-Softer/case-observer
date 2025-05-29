package ro.signsofter.caseobserver.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "hearing")
public class Hearing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "case_id", nullable = false)
    @JsonBackReference
    private CourtCase courtCase;

    @Column(name = "judicial_panel")
    private String judicialPanel;

    @Column(name = "solution")
    private String solution;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "hearing_date", nullable = false)
    private LocalDateTime hearingDate;

    @Column(name = "pronouncement_date", nullable = false)
    private LocalDateTime pronouncementDate;
}
