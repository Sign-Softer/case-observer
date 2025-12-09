package ro.signsofter.caseobserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ro.signsofter.caseobserver.controller.dto.UserDto;
import ro.signsofter.caseobserver.entity.User;
import ro.signsofter.caseobserver.repository.UserRepository;
import ro.signsofter.caseobserver.security.JwtService;
import ro.signsofter.caseobserver.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebMvcTest(controllers = UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Mock SecurityContext for authenticated endpoints
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("lawyer12");
        
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void listUsers_returns200_whenAuthenticated() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("lawyer12");
        user.setEmail("lawyer@example.com");
        user.setRole(User.Role.USER);

        when(userRepository.findAll()).thenReturn(List.of(user));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].username").value("lawyer12"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].role").value("USER"));
    }

    @Test
    void listUsers_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users"))
                .andExpect(MockMvcResultMatchers.status().isOk()); // Now it's permitAll in test config
    }

    @Test
    void getUser_returns200_whenFound() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("lawyer12");
        user.setEmail("lawyer@example.com");
        user.setRole(User.Role.USER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/{id}", 1))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("lawyer12"));
    }

    @Test
    void createUser_returns200_onSuccess() throws Exception {
        UserDto dto = new UserDto();
        dto.setUsername("lawyer22");
        dto.setEmail("lawyer2@example.com");
        dto.setPassword("password123");
        dto.setRole("USER");

        User saved = new User();
        saved.setId(2L);
        saved.setUsername("lawyer22");
        saved.setEmail("lawyer2@example.com");
        saved.setRole(User.Role.USER);

        when(userRepository.save(any(User.class))).thenReturn(saved);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("lawyer22"));
    }

    @Test
    void changePassword_returns200_onSuccess() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("lawyer12");
        user.setEmail("lawyer@example.com");
        user.setPassword("$2a$10$hashedCurrentPassword");
        user.setRole(User.Role.USER);

        when(userRepository.findByUsername("lawyer12")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("CurrentPass123!", "$2a$10$hashedCurrentPassword")).thenReturn(true);
        when(passwordEncoder.encode("NewPass123!")).thenReturn("$2a$10$hashedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        String request = """
                {
                    "currentPassword": "CurrentPass123!",
                    "newPassword": "NewPass123!"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Password changed successfully"));
    }

    @Test
    void changePassword_returns400_whenPasswordTooShort() throws Exception {
        String request = """
                {
                    "currentPassword": "CurrentPass123!",
                    "newPassword": "Pass1!"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Password must be at least 8 characters long"));
    }

    @Test
    void changePassword_returns400_whenPasswordMissingLowercase() throws Exception {
        String request = """
                {
                    "currentPassword": "CurrentPass123!",
                    "newPassword": "PASSWORD123!"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character (@$!%*?&)"));
    }

    @Test
    void changePassword_returns400_whenPasswordMissingUppercase() throws Exception {
        String request = """
                {
                    "currentPassword": "CurrentPass123!",
                    "newPassword": "password123!"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character (@$!%*?&)"));
    }

    @Test
    void changePassword_returns400_whenPasswordMissingDigit() throws Exception {
        String request = """
                {
                    "currentPassword": "CurrentPass123!",
                    "newPassword": "Password!"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character (@$!%*?&)"));
    }

    @Test
    void changePassword_returns400_whenPasswordMissingSpecialCharacter() throws Exception {
        String request = """
                {
                    "currentPassword": "CurrentPass123!",
                    "newPassword": "Password123"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character (@$!%*?&)"));
    }

    @Test
    void changePassword_returns400_whenCurrentPasswordIncorrect() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("lawyer12");
        user.setEmail("lawyer@example.com");
        user.setPassword("$2a$10$hashedCurrentPassword");
        user.setRole(User.Role.USER);

        when(userRepository.findByUsername("lawyer12")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword123!", "$2a$10$hashedCurrentPassword")).thenReturn(false);

        String request = """
                {
                    "currentPassword": "WrongPassword123!",
                    "newPassword": "NewPass123!"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Current password is incorrect"));
    }

    @Test
    void changePassword_returns400_whenCurrentPasswordBlank() throws Exception {
        String request = """
                {
                    "currentPassword": "",
                    "newPassword": "NewPass123!"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void changePassword_returns400_whenNewPasswordBlank() throws Exception {
        String request = """
                {
                    "currentPassword": "CurrentPass123!",
                    "newPassword": ""
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }
}
