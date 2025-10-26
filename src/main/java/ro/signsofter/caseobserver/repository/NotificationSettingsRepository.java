package ro.signsofter.caseobserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ro.signsofter.caseobserver.entity.NotificationSettings;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationSettingsRepository extends JpaRepository<NotificationSettings, Long> {

    Optional<NotificationSettings> findByCourtCaseId(Long courtCaseId);

    @Query("SELECT ns FROM NotificationSettings ns WHERE ns.courtCase.monitoringEnabled = true AND ns.nextCheckAt <= :currentTime")
    List<NotificationSettings> findSettingsReadyForCheck(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT ns FROM NotificationSettings ns WHERE ns.courtCase.monitoringEnabled = true")
    List<NotificationSettings> findAllActiveMonitoringSettings();

    @Query("SELECT ns FROM NotificationSettings ns WHERE ns.courtCase.id = :caseId AND ns.courtCase.monitoringEnabled = true")
    Optional<NotificationSettings> findActiveSettingsByCaseId(@Param("caseId") Long caseId);

    boolean existsByCourtCaseId(Long courtCaseId);
}
