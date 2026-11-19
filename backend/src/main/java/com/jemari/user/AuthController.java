package com.jemari.user;

import com.jemari.common.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email") != null ? request.get("email").trim().toLowerCase(Locale.ROOT) : "";
        String password = request.get("password") != null ? request.get("password") : "";

        if (email.isEmpty() || password.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email dan kata sandi wajib diisi."));
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !user.isActive() || !passwordEncoder.matches(password, user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Email atau kata sandi salah."));
        }

        String roleName = user.getRole().name();
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getId(), roleName);

        return ResponseEntity.ok(buildAuthResponse(token, user));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> request) {
        String email = request.get("email") != null ? String.valueOf(request.get("email")).trim().toLowerCase(Locale.ROOT) : "";
        String password = request.get("password") != null ? String.valueOf(request.get("password")) : "";
        String rawRole = request.get("role") != null ? String.valueOf(request.get("role")).trim().toUpperCase(Locale.ROOT) : "UMKM";

        if (email.isEmpty() || password.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email dan kata sandi wajib diisi."));
        }

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Email sudah terdaftar. Silakan masuk."));
        }

        Role role;
        try {
            role = Role.valueOf(rawRole.replace("ROLE_", ""));
        } catch (IllegalArgumentException e) {
            role = Role.UMKM;
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setActive(true);

        User saved = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(saved.getEmail(), saved.getId(), saved.getRole().name());

        return ResponseEntity.status(HttpStatus.CREATED).body(buildAuthResponse(token, saved));
    }

    private Map<String, Object> buildAuthResponse(String token, User user) {
        Map<String, Object> userMap = new LinkedHashMap<>();
        userMap.put("id", user.getId());
        userMap.put("email", user.getEmail());
        userMap.put("role", user.getRole().name());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("token", token);
        response.put("accessToken", token);
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("role", user.getRole().name());
        response.put("user", userMap);
        return response;
    }
}