package ro.signsofter.caseobserver.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.signsofter.caseobserver.entity.CourtCase;
import ro.signsofter.caseobserver.entity.NotificationSettings;
import ro.signsofter.caseobserver.entity.User;
import ro.signsofter.caseobserver.entity.UserCase;
import ro.signsofter.caseobserver.exception.portal.PortalQueryException;
import ro.signsofter.caseobserver.external.PortalQueryService;
import ro.signsofter.caseobserver.external.dto.caseResponse.CaseDetailsDto;
import ro.signsofter.caseobserver.repository.CourtCaseRepository;
import ro.signsofter.caseobserver.repository.NotificationSettingsRepository;
import ro.signsofter.caseobserver.repository.UserCaseRepository;
import ro.signsofter.caseobserver.service.CaseChangeDetectorService;
import ro.signsofter.caseobserver.service.NotificationService;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration test to verify the complete monitoring workflow:
 * 1. Start monitoring a case
 * 2. Check for updates (with actual change detection)
 * 3. Verify notifications are sent when changes are detected
 */
@ExtendWith(MockitoExtension.class)
class MonitoringIntegrationTest {

    @Mock
    private CourtCaseRepository courtCaseRepository;
    
    @Mock
    private NotificationSettingsRepository notificationSettingsRepository;
    
    @Mock
    private UserCaseRepository userCaseRepository;
    
    @Mock
    private PortalQueryService portalQueryService;
    
    @Mock
    private CaseChangeDetectorService caseChangeDetectorService;
    
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CaseMonitoringServiceImpl caseMonitoringService;

    private CourtCase testCase;
    private NotificationSettings testSettings;
    private User testUser;
    private UserCase testUserCase;
    private CaseDetailsDto initialCaseData;
    private CaseDetailsDto updatedCaseData;

    @BeforeEach
    void setUp() {
        // Setup test case (initial state)
        testCase = new CourtCase();
        testCase.setId(1L);
        testCase.setCaseNumber("12345/2025");
        testCase.setCourtName("TRIBUNALUL_BUCURESTI");
        testCase.setStatus("Fond");
        testCase.setProceduralStage("Fond");
        testCase.setMonitoringEnabled(false);

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("lawyer12");
        testUser.setEmail("lawyer1@example.com");

        // Setup test user case
        testUserCase = new UserCase();
        testUserCase.setUser(testUser);
        testUserCase.setCourtCase(testCase);

        // Initial case data from portal
        initialCaseData = new CaseDetailsDto();
        initialCaseData.setNumber("12345/2025");
        initialCaseData.setInstitution("TRIBUNALUL_BUCURESTI");
        initialCaseData.setProceduralStage("Fond");
        initialCaseData.setCaseCategory("Civil");
        initialCaseData.setSubject("Contract dispute");
        initialCaseData.setDepartment("Civil");
        initialCaseData.setHearings(Collections.emptyList());
        initialCaseData.setParties(Collections.emptyList());

        // Updated case data (with changes)
        updatedCaseData = new CaseDetailsDto();
        updatedCaseData.setNumber("12345/2025");
        updatedCaseData.setInstitution("TRIBUNALUL_BUCURESTI");
        updatedCaseData.setProceduralStage("Procedura"); // Changed
        updatedCaseData.setCaseCategory("Civil");
        updatedCaseData.setSubject("Contract dispute");
        updatedCaseData.setDepartment("Civil");
        updatedCaseData.setHearings(Collections.emptyList());
        updatedCaseData.setParties(Collections.emptyList());

        // Setup notification settings
        testSettings = new NotificationSettings();
        testSettings.setId(1L);
        testSettings.setCourtCase(testCase);
        testSettings.setNotificationIntervalMinutes(60);
        testSettings.setEmailEnabled(true);
        testSettings.setSmsEnabled(false);
        testSettings.setNotifyOnStatusChanges(true);
        testSettings.setNotifyOnHearingChanges(true);
        testSettings.setNotifyOnPartyChanges(true);
        testSettings.setNotifyOnProceduralStageChanges(true);
    }

    @Test
    void testCompleteMonitoringWorkflow_DetectsChangesAndSendsNotification() throws PortalQueryException {
        // STEP 1: Start monitoring the case
        when(courtCaseRepository.findById(1L)).thenReturn(Optional.of(testCase));
        when(notificationSettingsRepository.findByCourtCaseId(1L)).thenReturn(Optional.empty());
        
        caseMonitoringService.startMonitoringCase(1L, 60);
        
        // Verify monitoring was started
        verify(courtCaseRepository).findById(1L);
        verify(courtCaseRepository).save(argThat(CourtCase::getMonitoringEnabled));
        verify(notificationSettingsRepository).save(any(NotificationSettings.class));

        // STEP 2: Check for updates with detected changes
        // Mock the dependencies for checkCaseForUpdates
        when(courtCaseRepository.findById(1L)).thenReturn(Optional.of(testCase));
        when(notificationSettingsRepository.findByCourtCaseId(1L)).thenReturn(Optional.of(testSettings));
        when(userCaseRepository.findByCourtCaseId(1L)).thenReturn(Collections.singletonList(testUserCase));
        when(portalQueryService.fetchCaseDetails(testCase.getCaseNumber(), testCase.getCourtName()))
                .thenReturn(updatedCaseData);

        // Create realistic change detection result
        CaseChangeDetectorService.CaseChanges changes = new CaseChangeDetectorService.CaseChanges();
        changes.setHasChanges(true);
        changes.setStatusChanged(true);
        changes.setProceduralStageChanged(true);
        changes.setOldStatus("Fond");
        changes.setNewStatus("Procedura");
        changes.setOldProceduralStage("Fond");
        changes.setNewProceduralStage("Procedura");
        
        when(caseChangeDetectorService.detectChanges(testCase, updatedCaseData)).thenReturn(changes);

        // STEP 3: Check for updates
        caseMonitoringService.checkCaseForUpdates(1L);

        // STEP 4: Verify notification was sent
        verify(courtCaseRepository, atLeastOnce()).findById(1L);
        verify(notificationSettingsRepository, atLeastOnce()).findByCourtCaseId(1L);
        verify(userCaseRepository).findByCourtCaseId(1L);
        verify(portalQueryService).fetchCaseDetails(anyString(), anyString());
        verify(caseChangeDetectorService).detectChanges(testCase, updatedCaseData);
        
        // Critical verification: notification was sent to the user
        verify(notificationService).sendCaseChangeNotification(
            eq(testUser), 
            eq(testCase), 
            eq(changes), 
            eq(testSettings)
        );
        
        // Verify settings were updated with next check time
        verify(notificationSettingsRepository, atLeastOnce()).save(any(NotificationSettings.class));
    }

    @Test
    void testMonitoringWorkflow_NoChanges_NoNotification() throws PortalQueryException {
        // Start monitoring
        when(courtCaseRepository.findById(1L)).thenReturn(Optional.of(testCase));
        when(notificationSettingsRepository.findByCourtCaseId(1L)).thenReturn(Optional.empty());
        
        caseMonitoringService.startMonitoringCase(1L, 60);

        // Check for updates with no changes
        reset(notificationSettingsRepository);
        when(courtCaseRepository.findById(1L)).thenReturn(Optional.of(testCase));
        when(notificationSettingsRepository.findByCourtCaseId(1L)).thenReturn(Optional.of(testSettings));
        when(portalQueryService.fetchCaseDetails(anyString(), anyString())).thenReturn(initialCaseData);
        
        CaseChangeDetectorService.CaseChanges noChanges = new CaseChangeDetectorService.CaseChanges();
        noChanges.setHasChanges(false);
        
        when(caseChangeDetectorService.detectChanges(testCase, initialCaseData)).thenReturn(noChanges);

        // Check for updates
        caseMonitoringService.checkCaseForUpdates(1L);

        // Verify NO notification was sent
        verify(notificationService, never()).sendCaseChangeNotification(any(), any(), any(), any());
        
        // But settings were still updated for next check
        verify(notificationSettingsRepository).save(testSettings);
    }

    @Test
    void testMonitoringWorkflow_UserPreferenceFiltering() throws PortalQueryException {
        // Setup: enable monitoring and user disabled status change notifications
        testCase.setMonitoringEnabled(true);
        testSettings.setNotifyOnStatusChanges(false);
        testSettings.setNotifyOnProceduralStageChanges(false);
        
        when(courtCaseRepository.findById(1L)).thenReturn(Optional.of(testCase));
        when(notificationSettingsRepository.findByCourtCaseId(1L)).thenReturn(Optional.of(testSettings));
        when(portalQueryService.fetchCaseDetails(anyString(), anyString())).thenReturn(initialCaseData);
        
        CaseChangeDetectorService.CaseChanges changes = new CaseChangeDetectorService.CaseChanges();
        changes.setHasChanges(false);
        
        when(caseChangeDetectorService.detectChanges(testCase, initialCaseData)).thenReturn(changes);

        caseMonitoringService.checkCaseForUpdates(1L);

        // Since user disabled status notifications, NO notification should be sent
        verify(notificationService, never()).sendCaseChangeNotification(any(), any(), any(), any());
        // But the case was still checked
        verify(courtCaseRepository).findById(1L);
        verify(notificationSettingsRepository).findByCourtCaseId(1L);
        verify(portalQueryService).fetchCaseDetails(anyString(), anyString());
        verify(caseChangeDetectorService).detectChanges(testCase, initialCaseData);
    }
}
