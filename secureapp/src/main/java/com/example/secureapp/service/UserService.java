package com.example.secureapp.service;
import com.example.secureapp.model.User;
import com.example.secureapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service @RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void register(List<User> users) {
        users.forEach(u -> {
            u.setPassword(passwordEncoder.encode(u.getPassword()));
            userRepository.save(u);
        });
    }

    public boolean validateLogin(String email, String pass) {
        return userRepository.findByEmail(email)
                .map(u -> passwordEncoder.matches(pass, u.getPassword()))
                .orElse(false);
    }

    public User getByEmail(String email) { return userRepository.findByEmail(email).orElseThrow(); }
    public List<User> getAllUsers() { return userRepository.findAll(); }
    public User getUserById(Long id) { return userRepository.findById(id).orElseThrow(); }
    public void deleteUser(Long id) { userRepository.deleteById(id); }
}