package ro.signsofter.caseobserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.signsofter.caseobserver.entity.CourtCase;
import ro.signsofter.caseobserver.entity.NotificationSettings;
import ro.signsofter.caseobserver.entity.UserCase;
import ro.signsofter.caseobserver.exception.portal.PortalQueryException;
import ro.signsofter.caseobserver.external.PortalQueryService;
import ro.signsofter.caseobserver.external.dto.caseResponse.CaseDetailsDto;
import ro.signsofter.caseobserver.repository.CourtCaseRepository;
import ro.signsofter.caseobserver.repository.NotificationSettingsRepository;
import ro.signsofter.caseobserver.repository.UserCaseRepository;
import ro.signsofter.caseobserver.service.CaseChangeDetectorService;
import ro.signsofter.caseobserver.service.CaseMonitoringService;
import ro.signsofter.caseobserver.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CaseMonitoringServiceImpl implements CaseMonitoringService {

    @Autowired
    private NotificationSettingsRepository notificationSettingsRepository;
    
    @Autowired
    private CourtCaseRepository courtCaseRepository;
    
    @Autowired
    private UserCaseRepository userCaseRepository;
    
    @Autowired
    private PortalQueryService portalQueryService;
    
    @Autowired
    private CaseChangeDetectorService caseChangeDetectorService;
    
    @Autowired
    private NotificationService notificationService;

    @Override
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void checkAllMonitoredCases() {
        System.out.println("Starting scheduled case monitoring check at " + LocalDateTime.now());
        
        try {
            List<NotificationSettings> settingsToCheck = notificationSettingsRepository.findSettingsReadyForCheck(LocalDateTime.now());
            
            System.out.println("Found " + settingsToCheck.size() + " cases ready for monitoring check");
            
            for (NotificationSettings settings : settingsToCheck) {
                try {
                    checkCaseForUpdates(settings.getCourtCase().getId());
                } catch (Exception e) {
                    System.err.println("Error checking case " + settings.getCourtCase().getId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("Completed scheduled case monitoring check");
        } catch (Exception e) {
            System.err.println("Error in scheduled monitoring check: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    @Transactional
    public void checkCaseForUpdates(Long caseId) throws PortalQueryException {
        CourtCase courtCase = courtCaseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Case not found with id " + caseId));
        
        if (!courtCase.getMonitoringEnabled()) {
            System.out.println("Case " + caseId + " monitoring is disabled, skipping check");
            return;
        }
        
        NotificationSettings settings = notificationSettingsRepository.findByCourtCaseId(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Notification settings not found for case " + caseId));
        
        System.out.println("Checking case " + caseId + " (" + courtCase.getCaseNumber() + ") for updates");
        
        // Fetch latest data from portal
        CaseDetailsDto latestData = portalQueryService.fetchCaseDetails(courtCase.getCaseNumber(), courtCase.getCourtName());
        
        if (latestData == null) {
            System.out.println("Failed to fetch latest data for case " + caseId);
            return;
        }
        
        // Detect changes
        CaseChangeDetectorService.CaseChanges changes = caseChangeDetectorService.detectChanges(courtCase, latestData);
        
        if (changes.hasAnyChanges()) {
            System.out.println("Changes detected for case " + caseId + ": " + changes.hasAnyChanges());
            
            // Get all users monitoring this case
            List<UserCase> userCases = userCaseRepository.findByCourtCaseId(caseId);
            
            for (UserCase userCase : userCases) {
                // Check if user wants to be notified about these specific changes
                if (shouldNotifyUser(changes, settings)) {
                    notificationService.sendCaseChangeNotification(
                        userCase.getUser(), 
                        courtCase, 
                        changes, 
                        settings
                    );
                }
            }
            
            // Update the case with latest data
            updateCaseWithLatestData(courtCase, latestData);
        } else {
            System.out.println("No changes detected for case " + caseId);
        }
        
        // Update last checked time and schedule next check
        settings.setLastCheckedAt(LocalDateTime.now());
        settings.calculateNextCheckTime();
        notificationSettingsRepository.save(settings);
        
        System.out.println("Completed check for case " + caseId + ", next check scheduled for " + settings.getNextCheckAt());
    }

    @Override
    @Transactional
    public void startMonitoringCase(Long caseId, Integer notificationIntervalMinutes) {
        CourtCase courtCase = courtCaseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Case not found with id " + caseId));
        
        // Enable monitoring on the case
        courtCase.setMonitoringEnabled(true);
        courtCaseRepository.save(courtCase);
        
        // Create or update notification settings
        NotificationSettings settings = notificationSettingsRepository.findByCourtCaseId(caseId)
                .orElse(new NotificationSettings());
        
        settings.setCourtCase(courtCase);
        settings.setNotificationIntervalMinutes(notificationIntervalMinutes);
        settings.setLastCheckedAt(LocalDateTime.now());
        settings.calculateNextCheckTime();
        
        notificationSettingsRepository.save(settings);
        
        System.out.println("Started monitoring case " + caseId + " with " + notificationIntervalMinutes + " minute intervals");
    }

    @Override
    @Transactional
    public void stopMonitoringCase(Long caseId) {
        CourtCase courtCase = courtCaseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Case not found with id " + caseId));
        
        // Disable monitoring on the case
        courtCase.setMonitoringEnabled(false);
        courtCaseRepository.save(courtCase);
        
        System.out.println("Stopped monitoring case " + caseId);
    }

    @Override
    @Transactional
    public void updateNotificationSettings(Long caseId, NotificationSettings newSettings) {
        NotificationSettings existingSettings = notificationSettingsRepository.findByCourtCaseId(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Notification settings not found for case " + caseId));
        
        // Update settings
        existingSettings.setNotificationIntervalMinutes(newSettings.getNotificationIntervalMinutes());
        existingSettings.setEmailEnabled(newSettings.getEmailEnabled());
        existingSettings.setSmsEnabled(newSettings.getSmsEnabled());
        existingSettings.setNotifyOnHearingChanges(newSettings.getNotifyOnHearingChanges());
        existingSettings.setNotifyOnStatusChanges(newSettings.getNotifyOnStatusChanges());
        existingSettings.setNotifyOnPartyChanges(newSettings.getNotifyOnPartyChanges());
        existingSettings.setNotifyOnProceduralStageChanges(newSettings.getNotifyOnProceduralStageChanges());
        
        // Recalculate next check time if interval changed
        if (!existingSettings.getNotificationIntervalMinutes().equals(newSettings.getNotificationIntervalMinutes())) {
            existingSettings.calculateNextCheckTime();
        }
        
        notificationSettingsRepository.save(existingSettings);
        
        System.out.println("Updated notification settings for case " + caseId);
    }

    @Override
    public List<NotificationSettings> getActiveMonitoringSettings() {
        return notificationSettingsRepository.findAllActiveMonitoringSettings();
    }

    @Override
    public boolean isCaseBeingMonitored(Long caseId) {
        Optional<NotificationSettings> settings = notificationSettingsRepository.findByCourtCaseId(caseId);
        return settings.isPresent() && settings.get().getCourtCase().getMonitoringEnabled();
    }

    @Override
    @Transactional
    public void scheduleNextCheck(Long caseId) {
        NotificationSettings settings = notificationSettingsRepository.findByCourtCaseId(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Notification settings not found for case " + caseId));
        
        settings.calculateNextCheckTime();
        notificationSettingsRepository.save(settings);
        
        System.out.println("Scheduled next check for case " + caseId + " at " + settings.getNextCheckAt());
    }
    
    private boolean shouldNotifyUser(CaseChangeDetectorService.CaseChanges changes, NotificationSettings settings) {
        if (changes.isStatusChanged() && settings.getNotifyOnStatusChanges()) {
            return true;
        }
        if (changes.isProceduralStageChanged() && settings.getNotifyOnProceduralStageChanges()) {
            return true;
        }
        if (changes.isHearingsChanged() && settings.getNotifyOnHearingChanges()) {
            return true;
        }
        if (changes.isPartiesChanged() && settings.getNotifyOnPartyChanges()) {
            return true;
        }
        return false;
    }
    
    private void updateCaseWithLatestData(CourtCase courtCase, CaseDetailsDto latestData) {
        // Update basic fields
        courtCase.setStatus(latestData.getProceduralStage());
        courtCase.setCategory(latestData.getCaseCategory());
        courtCase.setDepartment(latestData.getDepartment());
        courtCase.setSubject(latestData.getSubject());
        courtCase.setProceduralStage(latestData.getProceduralStage());
        
        // TODO: Update hearings and parties if needed
        // This would require more complex logic to handle additions, removals, and updates
        
        courtCaseRepository.save(courtCase);
    }
}
