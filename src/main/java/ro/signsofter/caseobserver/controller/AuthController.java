package ro.signsofter.caseobserver.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ro.signsofter.caseobserver.entity.User;
import ro.signsofter.caseobserver.repository.UserRepository;
import ro.signsofter.caseobserver.security.JwtService;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest req) {
        return userRepository.findByUsername(req.username())
                .filter(u -> passwordEncoder.matches(req.password(), u.getPassword()))
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(Map.of(
                        "accessToken", jwtService.generateAccessToken(u.getUsername(), u.getRole().name()),
                        "refreshToken", jwtService.generateRefreshToken(u.getUsername(), u.getRole().name()),
                        "tokenType", "Bearer",
                        "role", u.getRole().name()
                )))
                .orElseGet(() -> ResponseEntity.status(401).body("Invalid credentials"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest req) {
        if (userRepository.findByUsername(req.username()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        if (userRepository.findByEmail(req.email()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        User u = new User();
        u.setUsername(req.username());
        u.setEmail(req.email());
        u.setPassword(passwordEncoder.encode(req.password()));
        u.setRole(User.Role.USER);
        userRepository.save(u);
        return ResponseEntity.ok(Map.of("message", "registered"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody @Valid RefreshRequest req) {
        try {
            var claims = jwtService.parse(req.refreshToken());
            if (!"refresh".equals(claims.get("typ", String.class))) {
                return ResponseEntity.status(401).body("Invalid refresh token");
            }
            String username = claims.getSubject();
            String role = claims.get("role", String.class);
            return ResponseEntity.ok(Map.of(
                    "accessToken", jwtService.generateAccessToken(username, role),
                    "tokenType", "Bearer"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid refresh token");
        }
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record RegisterRequest(@NotBlank String username, @NotBlank String email, @NotBlank String password) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}
}


