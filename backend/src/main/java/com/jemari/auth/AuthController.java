package com.jemari.auth;

import com.jemari.common.dto.ApiResponse;
import com.jemari.common.dto.LoginRequest;
import com.jemari.common.dto.LoginResponse;
import com.jemari.common.security.JwtTokenProvider;
import com.jemari.user.User;
import com.jemari.user.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @RequestBody LoginRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Pengguna tidak ditemukan"));

            String token = jwtTokenProvider.generateToken(
                    user.getEmail(),
                    user.getId(),
                    user.getRole().name()
            );

            LoginResponse loginResponse = new LoginResponse(
                    token,
                    user.getEmail(),
                    user.getRole().name()
            );

            return ResponseEntity.ok(
                    ApiResponse.ok("Login berhasil", loginResponse)
            );

        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Email atau password salah, atau akun tidak aktif"));
        }
    }
}