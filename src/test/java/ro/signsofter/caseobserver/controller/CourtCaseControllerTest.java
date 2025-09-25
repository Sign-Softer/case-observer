package ro.signsofter.caseobserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ro.signsofter.caseobserver.controller.dto.CreateCaseRequestDto;
import ro.signsofter.caseobserver.entity.CourtCase;
import ro.signsofter.caseobserver.exception.portal.PortalQueryException;
import ro.signsofter.caseobserver.external.PortalQueryService;
import ro.signsofter.caseobserver.external.dto.caseResponse.CaseDetailsDto;
import ro.signsofter.caseobserver.external.dto.caseResponse.HearingDto;
import ro.signsofter.caseobserver.external.dto.caseResponse.PartyDto;
import ro.signsofter.caseobserver.repository.CourtCaseRepository;
import ro.signsofter.caseobserver.repository.UserCaseRepository;
import ro.signsofter.caseobserver.repository.UserRepository;
import ro.signsofter.caseobserver.security.JwtService;
import ro.signsofter.caseobserver.service.CourtCaseService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = CourtCaseController.class)
@Import(TestSecurityConfig.class)
class CourtCaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private CourtCaseService courtCaseService;

    @MockBean private PortalQueryService portalQueryService;
    @MockBean private CourtCaseRepository courtCaseRepository;
    @MockBean private UserRepository userRepository;
    @MockBean private UserCaseRepository userCaseRepository;
    @MockBean private JwtService jwtService;

    @Test
    void createCase_returns400_whenValidationFails() throws Exception {
        // Empty body triggers @NotBlank on DTO
        mockMvc.perform(MockMvcRequestBuilders.post("/api/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void createCase_returns200_andDto_onSuccess() throws Exception {
        CreateCaseRequestDto req = new CreateCaseRequestDto();
        req.setCaseNumber("12345/2025");
        req.setInstitution("TRIBUNALUL BUCURESTI");
        req.setCaseName("Popescu vs Ionescu");

        // Arrange service dependencies
        HearingDto hearingDto = new HearingDto();
        hearingDto.setDate("2025-09-01");
        hearingDto.setTime("10:30");

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
        details.setHearings(java.util.List.of(hearingDto));
        details.setParties(java.util.List.of(partyDto));

        when(portalQueryService.fetchCaseDetails(any(), any())).thenReturn(details);
        when(courtCaseRepository.existsByCaseNumber(any())).thenReturn(false);
        when(courtCaseRepository.save(any(CourtCase.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.caseNumber").value("12345/2025"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.courtName").value("TRIBUNALUL BUCURESTI"));
    }

    @Test
    void fetchCaseData_returns400_onPortalError() throws Exception {
        when(portalQueryService.fetchCaseDetails(any(), any())).thenThrow(new PortalQueryException("portal down"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/cases/fetch")
                        .param("caseNumber", "12345/2025")
                        .param("institution", "TRIBUNALUL BUCURESTI"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void getCase_returns404_whenNotFound() throws Exception {
        when(courtCaseRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/cases/{id}", 999))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}


