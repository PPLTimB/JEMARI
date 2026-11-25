
package com.jemari.auth;

import com.jemari.auth.dto.RegisterRequest;
import com.jemari.auth.dto.LoginRequest;
import com.jemari.auth.dto.LoginResponse;
import com.jemari.auth.dto.AccountProfileResponse;
import com.jemari.auth.dto.UpdateAccountRequest;

import com.jemari.auth.service.AuthService;
import com.jemari.common.dto.ApiResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // REGISTER UMKM
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(
            @Valid @RequestBody RegisterRequest request) {

        authService.registerUmkm(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                        "Registrasi akun UMKM berhasil", null));
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.ok("Login berhasil", response));
    }

    // GET MY ACCOUNT
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AccountProfileResponse>> getMyAccount(
            Authentication authentication) {

        AccountProfileResponse response =
                authService.getMyAccount(authentication.getName());

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Data akun berhasil ditampilkan", response));
    }

    // UPDATE MY ACCOUNT
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<String>> updateMyAccount(
            Authentication authentication,
            @Valid @RequestBody UpdateAccountRequest request) {

        authService.updateMyAccount(
                authentication.getName(), request);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Data akun berhasil diperbarui", null));
    }

    // DEACTIVATE MY ACCOUNT
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<String>> deactivateMyAccount(
            Authentication authentication) {

        authService.deactivateMyAccount(authentication.getName());

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Akun berhasil dinonaktifkan", null));
    }
}
