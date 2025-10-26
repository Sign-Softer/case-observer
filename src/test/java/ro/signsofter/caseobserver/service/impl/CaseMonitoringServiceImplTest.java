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
import ro.signsofter.caseobserver.external.dto.caseResponse.CaseDetailsDto;
import ro.signsofter.caseobserver.external.dto.caseResponse.HearingDto;
import ro.signsofter.caseobserver.external.dto.caseResponse.PartyDto;
import ro.signsofter.caseobserver.repository.CourtCaseRepository;
import ro.signsofter.caseobserver.repository.NotificationSettingsRepository;
import ro.signsofter.caseobserver.repository.UserCaseRepository;
import ro.signsofter.caseobserver.service.CaseChangeDetectorService;
import ro.signsofter.caseobserver.service.CaseMonitoringService;
import ro.signsofter.caseobserver.service.NotificationService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseMonitoringServiceImplTest {

    @Mock
    private NotificationSettingsRepository notificationSettingsRepository;
    
    @Mock
    private CourtCaseRepository courtCaseRepository;
    
    @Mock
    private UserCaseRepository userCaseRepository;
    
    @Mock
    private CaseChangeDetectorService caseChangeDetectorService;
    
    @Mock
    private NotificationService notificationService;
    
    @Mock
    private ro.signsofter.caseobserver.external.PortalQueryService portalQueryService;

    @InjectMocks
    private CaseMonitoringServiceImpl caseMonitoringService;

    private CourtCase testCase;
    private NotificationSettings testSettings;
    private User testUser;
    private UserCase testUserCase;
    private CaseDetailsDto testCaseDetails;

    @BeforeEach
    void setUp() {
        // Setup test case
        testCase = new CourtCase();
        testCase.setId(1L);
        testCase.setCaseNumber("12345/2025");
        testCase.setCourtName("TRIBUNALUL_BUCURESTI");
        testCase.setStatus("Fond");
        testCase.setMonitoringEnabled(true);

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        // Setup test user case
        testUserCase = new UserCase();
        testUserCase.setUser(testUser);
        testUserCase.setCourtCase(testCase);

        // Setup test notification settings
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
        testSettings.setLastCheckedAt(LocalDateTime.now().minusHours(1));
        testSettings.setNextCheckAt(LocalDateTime.now().minusMinutes(30));

        // Setup test case details
        testCaseDetails = new CaseDetailsDto();
        testCaseDetails.setNumber("12345/2025");
        testCaseDetails.setInstitution("TRIBUNALUL_BUCURESTI");
        testCaseDetails.setProceduralStage("Procedura");
        testCaseDetails.setCaseCategory("Civil");
        testCaseDetails.setSubject("Contract dispute");
        testCaseDetails.setDepartment("Civil");
        testCaseDetails.setHearings(Collections.emptyList());
        testCaseDetails.setParties(Collections.emptyList());
    }

    @Test
    void testStartMonitoringCase() {
        // Given
        when(courtCaseRepository.findById(1L)).thenReturn(Optional.of(testCase));
        when(notificationSettingsRepository.findByCourtCaseId(1L)).thenReturn(Optional.empty());
        when(notificationSettingsRepository.save(any(NotificationSettings.class))).thenReturn(testSettings);

        // When
        caseMonitoringService.startMonitoringCase(1L, 120);

        // Then
        verify(courtCaseRepository).findById(1L);
        verify(courtCaseRepository).save(testCase);
        verify(notificationSettingsRepository).save(any(NotificationSettings.class));
        assertTrue(testCase.getMonitoringEnabled());
    }

    @Test
    void testStopMonitoringCase() {
        // Given
        when(courtCaseRepository.findById(1L)).thenReturn(Optional.of(testCase));

        // When
        caseMonitoringService.stopMonitoringCase(1L);

        // Then
        verify(courtCaseRepository).findById(1L);
        verify(courtCaseRepository).save(testCase);
        assertFalse(testCase.getMonitoringEnabled());
    }

    @Test
    void testCheckCaseForUpdates_WithChanges() throws PortalQueryException {
        // Given
        when(courtCaseRepository.findById(1L)).thenReturn(Optional.of(testCase));
        when(notificationSettingsRepository.findByCourtCaseId(1L)).thenReturn(Optional.of(testSettings));
        when(userCaseRepository.findByCourtCaseId(1L)).thenReturn(Arrays.asList(testUserCase));
        when(portalQueryService.fetchCaseDetails(anyString(), anyString())).thenReturn(testCaseDetails);
        
        CaseChangeDetectorService.CaseChanges changes = new CaseChangeDetectorService.CaseChanges();
        changes.setHasChanges(true);
        changes.setStatusChanged(true);
        changes.setOldStatus("Fond");
        changes.setNewStatus("Procedura");
        
        when(caseChangeDetectorService.detectChanges(testCase, testCaseDetails)).thenReturn(changes);

        // When
        caseMonitoringService.checkCaseForUpdates(1L);

        // Then
        verify(courtCaseRepository).findById(1L);
        verify(notificationSettingsRepository).findByCourtCaseId(1L);
        verify(caseChangeDetectorService).detectChanges(testCase, testCaseDetails);
        verify(notificationService).sendCaseChangeNotification(eq(testUser), eq(testCase), eq(changes), eq(testSettings));
        verify(notificationSettingsRepository).save(testSettings);
    }

    @Test
    void testCheckCaseForUpdates_NoChanges() throws PortalQueryException {
        // Given
        when(courtCaseRepository.findById(1L)).thenReturn(Optional.of(testCase));
        when(notificationSettingsRepository.findByCourtCaseId(1L)).thenReturn(Optional.of(testSettings));
        when(portalQueryService.fetchCaseDetails(anyString(), anyString())).thenReturn(testCaseDetails);
        
        CaseChangeDetectorService.CaseChanges changes = new CaseChangeDetectorService.CaseChanges();
        changes.setHasChanges(false);
        
        when(caseChangeDetectorService.detectChanges(testCase, testCaseDetails)).thenReturn(changes);

        // When
        caseMonitoringService.checkCaseForUpdates(1L);

        // Then
        verify(courtCaseRepository).findById(1L);
        verify(notificationSettingsRepository).findByCourtCaseId(1L);
        verify(caseChangeDetectorService).detectChanges(testCase, testCaseDetails);
        verify(notificationService, never()).sendCaseChangeNotification(any(), any(), any(), any());
        verify(notificationSettingsRepository).save(testSettings);
    }

    @Test
    void testCheckCaseForUpdates_MonitoringDisabled() throws PortalQueryException {
        // Given
        testCase.setMonitoringEnabled(false);
        when(courtCaseRepository.findById(1L)).thenReturn(Optional.of(testCase));

        // When
        caseMonitoringService.checkCaseForUpdates(1L);

        // Then
        verify(courtCaseRepository).findById(1L);
        verify(notificationSettingsRepository, never()).findByCourtCaseId(any());
        verify(caseChangeDetectorService, never()).detectChanges(any(), any());
        verify(notificationService, never()).sendCaseChangeNotification(any(), any(), any(), any());
    }

    @Test
    void testUpdateNotificationSettings() {
        // Given
        NotificationSettings newSettings = new NotificationSettings();
        newSettings.setNotificationIntervalMinutes(180);
        newSettings.setEmailEnabled(false);
        newSettings.setSmsEnabled(true);
        newSettings.setNotifyOnStatusChanges(false);
        newSettings.setNotifyOnHearingChanges(true);
        newSettings.setNotifyOnPartyChanges(false);
        newSettings.setNotifyOnProceduralStageChanges(true);

        when(notificationSettingsRepository.findByCourtCaseId(1L)).thenReturn(Optional.of(testSettings));
        when(notificationSettingsRepository.save(any(NotificationSettings.class))).thenReturn(testSettings);

        // When
        caseMonitoringService.updateNotificationSettings(1L, newSettings);

        // Then
        verify(notificationSettingsRepository).findByCourtCaseId(1L);
        verify(notificationSettingsRepository).save(testSettings);
        assertEquals(180, testSettings.getNotificationIntervalMinutes());
        assertFalse(testSettings.getEmailEnabled());
        assertTrue(testSettings.getSmsEnabled());
        assertFalse(testSettings.getNotifyOnStatusChanges());
        assertTrue(testSettings.getNotifyOnHearingChanges());
        assertFalse(testSettings.getNotifyOnPartyChanges());
        assertTrue(testSettings.getNotifyOnProceduralStageChanges());
    }

    @Test
    void testIsCaseBeingMonitored_True() {
        // Given
        when(notificationSettingsRepository.findByCourtCaseId(1L)).thenReturn(Optional.of(testSettings));

        // When
        boolean isMonitored = caseMonitoringService.isCaseBeingMonitored(1L);

        // Then
        assertTrue(isMonitored);
        verify(notificationSettingsRepository).findByCourtCaseId(1L);
    }

    @Test
    void testIsCaseBeingMonitored_False() {
        // Given
        when(notificationSettingsRepository.findByCourtCaseId(1L)).thenReturn(Optional.empty());

        // When
        boolean isMonitored = caseMonitoringService.isCaseBeingMonitored(1L);

        // Then
        assertFalse(isMonitored);
        verify(notificationSettingsRepository).findByCourtCaseId(1L);
    }

    @Test
    void testScheduleNextCheck() {
        // Given
        when(notificationSettingsRepository.findByCourtCaseId(1L)).thenReturn(Optional.of(testSettings));
        when(notificationSettingsRepository.save(any(NotificationSettings.class))).thenReturn(testSettings);

        // When
        caseMonitoringService.scheduleNextCheck(1L);

        // Then
        verify(notificationSettingsRepository).findByCourtCaseId(1L);
        verify(notificationSettingsRepository).save(testSettings);
        assertNotNull(testSettings.getNextCheckAt());
    }
}
