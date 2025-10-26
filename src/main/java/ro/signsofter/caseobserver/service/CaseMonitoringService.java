package ro.signsofter.caseobserver.service;

import ro.signsofter.caseobserver.entity.CourtCase;
import ro.signsofter.caseobserver.entity.NotificationSettings;
import ro.signsofter.caseobserver.exception.portal.PortalQueryException;

import java.util.List;

public interface CaseMonitoringService {
    
    void checkAllMonitoredCases();
    
    void checkCaseForUpdates(Long caseId) throws PortalQueryException;
    
    void startMonitoringCase(Long caseId, Integer notificationIntervalMinutes);
    
    void stopMonitoringCase(Long caseId);
    
    void updateNotificationSettings(Long caseId, NotificationSettings settings);
    
    List<NotificationSettings> getActiveMonitoringSettings();
    
    boolean isCaseBeingMonitored(Long caseId);
    
    void scheduleNextCheck(Long caseId);
}
