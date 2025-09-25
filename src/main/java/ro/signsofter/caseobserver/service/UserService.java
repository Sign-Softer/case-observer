package ro.signsofter.caseobserver.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ro.signsofter.caseobserver.entity.User;
import ro.signsofter.caseobserver.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        if (user.getPassword() != null && !user.getPassword().isBlank() && !isBCryptHash(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        if (userDetails.getPassword() != null && !userDetails.getPassword().isBlank()) {
            String newPassword = userDetails.getPassword();
            user.setPassword(isBCryptHash(newPassword) ? newPassword : passwordEncoder.encode(newPassword));
        }
        user.setRole(userDetails.getRole());
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    private boolean isBCryptHash(String raw) {
        return raw.startsWith("$2a$") || raw.startsWith("$2b$") || raw.startsWith("$2y$");
    }
}