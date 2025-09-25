package ro.signsofter.caseobserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ro.signsofter.caseobserver.entity.User;
import ro.signsofter.caseobserver.repository.UserRepository;
import ro.signsofter.caseobserver.security.JwtService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtService jwtService;

    @Test
    void register_returns200_onSuccess() throws Exception {
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode(any())).thenReturn("hashed");

        String request = """
                {
                    "username": "lawyer1",
                    "email": "lawyer@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("registered"));
    }

    @Test
    void register_returns400_whenUsernameExists() throws Exception {
        User existing = new User();
        existing.setUsername("lawyer1");
        when(userRepository.findByUsername("lawyer1")).thenReturn(Optional.of(existing));

        String request = """
                {
                    "username": "lawyer1",
                    "email": "lawyer@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Username already exists"));
    }

    @Test
    void login_returns200_withTokens_onValidCredentials() throws Exception {
        User user = new User();
        user.setUsername("lawyer1");
        user.setPassword("hashed");
        user.setRole(User.Role.USER);

        when(userRepository.findByUsername("lawyer1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(jwtService.generateAccessToken("lawyer1", "USER")).thenReturn("access-token");
        when(jwtService.generateRefreshToken("lawyer1", "USER")).thenReturn("refresh-token");

        String request = """
                {
                    "username": "lawyer1",
                    "password": "password123"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").value("access-token"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.role").value("USER"));
    }

    @Test
    void login_returns401_onInvalidCredentials() throws Exception {
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());

        String request = """
                {
                    "username": "lawyer1",
                    "password": "wrong"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void refresh_returns200_withNewAccessToken() throws Exception {
        // Create a mock Claims object using Mockito
        io.jsonwebtoken.Claims mockClaims = org.mockito.Mockito.mock(io.jsonwebtoken.Claims.class);
        when(mockClaims.getSubject()).thenReturn("lawyer1");
        when(mockClaims.get("typ", String.class)).thenReturn("refresh");
        when(mockClaims.get("role", String.class)).thenReturn("USER");
        
        when(jwtService.parse("refresh-token")).thenReturn(mockClaims);
        when(jwtService.generateAccessToken("lawyer1", "USER")).thenReturn("new-access-token");

        String request = """
                {
                    "refreshToken": "refresh-token"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").value("new-access-token"));
    }
}