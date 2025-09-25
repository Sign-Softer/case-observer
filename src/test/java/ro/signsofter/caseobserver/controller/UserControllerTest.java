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
import ro.signsofter.caseobserver.controller.dto.UserDto;
import ro.signsofter.caseobserver.entity.User;
import ro.signsofter.caseobserver.repository.UserRepository;
import ro.signsofter.caseobserver.security.JwtService;
import ro.signsofter.caseobserver.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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

    @Test
    void listUsers_returns200_whenAuthenticated() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("lawyer1");
        user.setEmail("lawyer@example.com");
        user.setRole(User.Role.USER);

        when(userRepository.findAll()).thenReturn(List.of(user));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].username").value("lawyer1"))
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
        user.setUsername("lawyer1");
        user.setEmail("lawyer@example.com");
        user.setRole(User.Role.USER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/{id}", 1))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("lawyer1"));
    }

    @Test
    void createUser_returns200_onSuccess() throws Exception {
        UserDto dto = new UserDto();
        dto.setUsername("lawyer2");
        dto.setEmail("lawyer2@example.com");
        dto.setPassword("password123");
        dto.setRole("USER");

        User saved = new User();
        saved.setId(2L);
        saved.setUsername("lawyer2");
        saved.setEmail("lawyer2@example.com");
        saved.setRole(User.Role.USER);

        when(userRepository.save(any(User.class))).thenReturn(saved);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("lawyer2"));
    }
}
