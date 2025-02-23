package ro.signsofter.caseobserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.signsofter.caseobserver.entity.UserCase;

@Repository
public interface UserCaseRepository extends JpaRepository<UserCase, UserCase.UserCaseId> {
}

