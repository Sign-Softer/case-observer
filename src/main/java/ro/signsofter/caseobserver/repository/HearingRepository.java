package ro.signsofter.caseobserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.signsofter.caseobserver.entity.Hearing;

import java.util.List;

@Repository
public interface HearingRepository extends JpaRepository<Hearing, Long> {

    List<Hearing> findByCourtCaseId(Long caseId);
}