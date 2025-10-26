package ro.signsofter.caseobserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.signsofter.caseobserver.entity.UserCase;

import java.util.List;

@Repository
public interface UserCaseRepository extends JpaRepository<UserCase, UserCase.UserCaseId> {
    List<UserCase> findByUserUsername(String username);
    List<UserCase> findByCourtCaseId(Long courtCaseId);
}

