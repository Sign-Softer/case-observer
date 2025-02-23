package ro.signsofter.caseobserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.signsofter.caseobserver.entity.CourtCase;

import java.util.Optional;

@Repository
public interface CourtCaseRepository extends JpaRepository<CourtCase, Long> {

    Optional<CourtCase> findByCaseId(String caseId);
}