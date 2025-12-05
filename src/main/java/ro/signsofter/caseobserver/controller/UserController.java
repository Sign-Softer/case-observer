package ro.signsofter.caseobserver.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ro.signsofter.caseobserver.controller.dto.UserDto;
import ro.signsofter.caseobserver.controller.dto.UserProfileDto;
import ro.signsofter.caseobserver.controller.mapper.UserMapper;
import ro.signsofter.caseobserver.entity.User;
import ro.signsofter.caseobserver.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<UserDto> listUsers() {
        return userService.getAllUsers().stream().map(UserMapper::toDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        Optional<User> u = userService.getUserById(id);
        return u.map(user -> ResponseEntity.ok(UserMapper.toDto(user))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get current authenticated user profile (only essential fields)
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<User> user = userService.getUserByUsername(username);
        return user.map(u -> {
            UserProfileDto dto = new UserProfileDto();
            dto.setId(u.getId());
            dto.setUsername(u.getUsername());
            dto.setEmail(u.getEmail());
            dto.setRole(u.getRole().name()); // Include role for frontend context
            return ResponseEntity.ok(dto);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Update current authenticated user profile
     */
    @PutMapping("/me")
    public ResponseEntity<UserProfileDto> updateCurrentUser(@Valid @RequestBody UpdateProfileRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User updated = userService.updateUserProfile(username, request.email());
        UserProfileDto dto = new UserProfileDto();
        dto.setId(updated.getId());
        dto.setUsername(updated.getUsername());
        dto.setEmail(updated.getEmail());
        dto.setRole(updated.getRole().name()); // Include role for frontend context
        return ResponseEntity.ok(dto);
    }

    /**
     * Change password for current authenticated user
     */
    @PutMapping("/me/password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        userService.changePassword(username, request.currentPassword(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody UserDto dto) {
        User saved = userService.createUser(UserMapper.fromDto(dto));
        return ResponseEntity.ok(UserMapper.toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> update(@PathVariable Long id, @Valid @RequestBody UserDto dto) {
        User updated = userService.updateUser(id, UserMapper.fromDto(dto));
        return ResponseEntity.ok(UserMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // mapping moved to UserMapper

    public record UpdateProfileRequest(@NotBlank String email) {}
    public record ChangePasswordRequest(@NotBlank String currentPassword, @NotBlank String newPassword) {}
}


