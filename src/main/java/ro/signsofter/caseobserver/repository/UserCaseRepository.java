package ro.signsofter.caseobserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ro.signsofter.caseobserver.entity.UserCase;

import java.util.List;

@Repository
public interface UserCaseRepository extends JpaRepository<UserCase, UserCase.UserCaseId> {
    List<UserCase> findByUserUsername(String username);
    List<UserCase> findByCourtCaseId(Long courtCaseId);

    @Query("SELECT uc FROM UserCase uc JOIN uc.courtCase cc WHERE uc.user.username = :username " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     LOWER(cc.caseNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(cc.imposedName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(cc.courtName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(cc.subject) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:status IS NULL OR :status = '' OR cc.status = :status) " +
           "AND (:monitoringEnabled IS NULL OR cc.monitoringEnabled = :monitoringEnabled) " +
           "AND (:courtName IS NULL OR :courtName = '' OR cc.courtName = :courtName)")
    List<UserCase> findCasesWithFilters(
            @Param("username") String username,
            @Param("search") String search,
            @Param("status") String status,
            @Param("monitoringEnabled") Boolean monitoringEnabled,
            @Param("courtName") String courtName,
            @Param("sortBy") String sortBy
    );
}

