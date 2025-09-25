package ro.signsofter.caseobserver.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.signsofter.caseobserver.controller.dto.CreateCaseRequestDto;
import ro.signsofter.caseobserver.entity.CourtCase;
import ro.signsofter.caseobserver.entity.Hearing;
import ro.signsofter.caseobserver.entity.Party;
import ro.signsofter.caseobserver.entity.UserCase;
import ro.signsofter.caseobserver.exception.portal.PortalQueryException;
import ro.signsofter.caseobserver.external.PortalQueryService;
import ro.signsofter.caseobserver.external.dto.caseResponse.CaseDetailsDto;
import ro.signsofter.caseobserver.external.dto.caseResponse.HearingDto;
import ro.signsofter.caseobserver.external.dto.caseResponse.PartyDto;
import ro.signsofter.caseobserver.repository.CourtCaseRepository;
import ro.signsofter.caseobserver.repository.UserCaseRepository;
import ro.signsofter.caseobserver.repository.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourtCaseServiceTest {

    @Mock private CourtCaseRepository courtCaseRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserCaseRepository userCaseRepository;
    @Mock private PortalQueryService portalQueryService;

    @InjectMocks private CourtCaseService courtCaseService;

    @Test
    void createCase_mapsExternalDataAndSaves() throws PortalQueryException {
        // Arrange external response
        when(courtCaseRepository.existsByCaseNumber(anyString())).thenReturn(false);
        HearingDto hearingDto = new HearingDto();
        hearingDto.setDate("2025-09-01");
        hearingDto.setTime("10:30");
        hearingDto.setSolution("Amana");
        hearingDto.setSummary("Short summary");
        hearingDto.setJudicialPanel("Panel 1");

        PartyDto partyDto = new PartyDto();
        partyDto.setName("John Doe");
        partyDto.setRole("Reclamant");

        CaseDetailsDto details = new CaseDetailsDto();
        details.setNumber("12345/2025");
        details.setInstitution("TRIBUNALUL BUCURESTI");
        details.setProceduralStage("Fond");
        details.setCaseCategory("Civil");
        details.setDepartment("Civil");
        details.setSubject("Litigiu");
        details.setHearings(List.of(hearingDto));
        details.setParties(List.of(partyDto));

        when(portalQueryService.fetchCaseDetails(anyString(), anyString())).thenReturn(details);
        when(courtCaseRepository.save(any(CourtCase.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateCaseRequestDto request = new CreateCaseRequestDto();
        request.setCaseNumber("12345/2025");
        request.setInstitution("TRIBUNALUL BUCURESTI");
        request.setCaseName("Popescu vs Ionescu");

        // Act
        CourtCase saved = courtCaseService.createCase(request);

        // Assert mapping
        assertThat(saved.getCaseNumber()).isEqualTo("12345/2025");
        assertThat(saved.getImposedName()).isEqualTo("Popescu vs Ionescu");
        assertThat(saved.getCourtName()).isEqualTo("TRIBUNALUL BUCURESTI");
        assertThat(saved.getProceduralStage()).isEqualTo("Fond");
        assertThat(saved.getCategory()).isEqualTo("Civil");
        assertThat(saved.getDepartment()).isEqualTo("Civil");
        assertThat(saved.getSubject()).isEqualTo("Litigiu");

        assertThat(saved.getHearings()).hasSize(1);
        Hearing h = saved.getHearings().get(0);
        assertThat(h.getJudicialPanel()).isEqualTo("Panel 1");
        assertThat(h.getSolution()).isEqualTo("Amana");
        assertThat(h.getDescription()).isEqualTo("Short summary");
        assertThat(h.getHearingDate()).isNotNull();
        assertThat(h.getPronouncementDate()).isNotNull();

        assertThat(saved.getParties()).hasSize(1);
        Party p = saved.getParties().get(0);
        assertThat(p.getName()).isEqualTo("John Doe");
        assertThat(p.getRole()).isEqualTo("Reclamant");

        // Verify save invoked
        ArgumentCaptor<CourtCase> captor = ArgumentCaptor.forClass(CourtCase.class);
        verify(courtCaseRepository).save(captor.capture());
        assertThat(captor.getValue().getCaseNumber()).isEqualTo("12345/2025");
    }

    @Test
    void getCasesForUser_returnsUserCases() {
        // Arrange
        String username = "testuser";
        
        CourtCase case1 = new CourtCase();
        case1.setId(1L);
        case1.setCaseNumber("12345/2025");
        case1.setCourtName("TRIBUNALUL BUCURESTI");
        
        CourtCase case2 = new CourtCase();
        case2.setId(2L);
        case2.setCaseNumber("67890/2025");
        case2.setCourtName("TRIBUNALUL CLUJ");
        
        UserCase userCase1 = new UserCase();
        userCase1.setCourtCase(case1);
        
        UserCase userCase2 = new UserCase();
        userCase2.setCourtCase(case2);
        
        List<UserCase> userCases = List.of(userCase1, userCase2);
        
        when(userCaseRepository.findByUserUsername(username)).thenReturn(userCases);
        
        // Act
        List<CourtCase> result = courtCaseService.getCasesForUser(username);
        
        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCaseNumber()).isEqualTo("12345/2025");
        assertThat(result.get(1).getCaseNumber()).isEqualTo("67890/2025");
        
        verify(userCaseRepository).findByUserUsername(username);
    }

    @Test
    void getCasesForUser_returnsEmptyList_whenUserHasNoCases() {
        // Arrange
        String username = "testuser";
        when(userCaseRepository.findByUserUsername(username)).thenReturn(List.of());
        
        // Act
        List<CourtCase> result = courtCaseService.getCasesForUser(username);
        
        // Assert
        assertThat(result).isEmpty();
        verify(userCaseRepository).findByUserUsername(username);
    }
}


