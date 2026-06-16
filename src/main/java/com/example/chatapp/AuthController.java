package com.example.chatapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.security.Principal; // <-- Required for the /me endpoint

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private ChatUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- METHOD 1: REGISTER ---
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam String username, @RequestParam String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists!");
        }

        ChatUser newUser = new ChatUser();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));

        userRepository.save(newUser);
        return ResponseEntity.ok("Registration successful! You can now log in.");
    } // <-- Notice the register method completely ends here!

    // --- METHOD 2: GET CURRENT USER ---
    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser(Principal principal) {
        if (principal != null) {
            return ResponseEntity.ok(principal.getName());
        }
        return ResponseEntity.status(401).body("Not authenticated");
    }
}