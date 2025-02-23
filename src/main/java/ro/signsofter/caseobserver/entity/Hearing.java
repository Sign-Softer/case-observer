package ro.signsofter.caseobserver.entity;

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
    private CourtCase courtCase;

    @Column(name = "hearing_date", nullable = false)
    private LocalDateTime hearingDate;

    @Column(name = "solution")
    private String solution;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
