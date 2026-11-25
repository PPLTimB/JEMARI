
package com.jemari.auth.service;

import com.jemari.auth.dto.RegisterRequest;
import com.jemari.auth.dto.LoginRequest;
import com.jemari.auth.dto.LoginResponse;
import com.jemari.auth.dto.AccountProfileResponse;
import com.jemari.auth.dto.UpdateAccountRequest;

import com.jemari.common.security.JwtTokenProvider;
import com.jemari.common.exception.AppException;

import com.jemari.umkm.UmkmProfile;
import com.jemari.umkm.UmkmProfileRepository;

import com.jemari.user.Role;
import com.jemari.user.User;
import com.jemari.user.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UmkmProfileRepository umkmProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            UserRepository userRepository,
            UmkmProfileRepository umkmProfileRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.umkmProfileRepository = umkmProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // REGISTER UMKM
    @Transactional
    public void registerUmkm(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(
                    "Email sudah terdaftar", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.UMKM);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        UmkmProfile profile = new UmkmProfile();
        profile.setUser(savedUser);
        profile.setBusinessName(request.getBusinessName());
        profile.setCategory(request.getCategory());
        profile.setBusinessScale(request.getBusinessScale());
        profile.setDescription(request.getDescription());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setContactEmail(request.getContactEmail());
        profile.setActive(true);

        umkmProfileRepository.save(profile);
    }

    // LOGIN
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(
                        "Email atau password salah",
                        HttpStatus.UNAUTHORIZED));

        if (!user.isActive()) {
            throw new AppException(
                    "Akun sudah tidak aktif", HttpStatus.FORBIDDEN);
        }

        if (!passwordEncoder.matches(
                request.getPassword(), user.getPasswordHash())) {
            throw new AppException(
                    "Email atau password salah",
                    HttpStatus.UNAUTHORIZED);
        }

        String token = jwtTokenProvider.generateToken(
                user.getEmail(),
                user.getId(),
                user.getRole().name());

        return new LoginResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getRole().name());
    }

    // GET MY ACCOUNT
    public AccountProfileResponse getMyAccount(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(
                        "Akun tidak ditemukan", HttpStatus.NOT_FOUND));

        if (!user.isActive()) {
            throw new AppException(
                    "Akun sudah tidak aktif", HttpStatus.FORBIDDEN);
        }

        if (user.getRole() != Role.UMKM) {
            throw new AppException(
                    "Akses hanya untuk akun UMKM", HttpStatus.FORBIDDEN);
        }

        UmkmProfile profile = umkmProfileRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new AppException(
                        "Profil usaha tidak ditemukan",
                        HttpStatus.NOT_FOUND));

        if (!profile.isActive()) {
            throw new AppException(
                    "Profil usaha sudah tidak aktif",
                    HttpStatus.FORBIDDEN);
        }

        return new AccountProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                profile.getBusinessName(),
                profile.getCategory(),
                profile.getBusinessScale(),
                profile.getDescription(),
                profile.getPhoneNumber(),
                profile.getContactEmail());
    }

    // UPDATE MY ACCOUNT
    @Transactional
    public void updateMyAccount(
            String email, UpdateAccountRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(
                        "Akun tidak ditemukan", HttpStatus.NOT_FOUND));

        if (!user.isActive()) {
            throw new AppException(
                    "Akun sudah tidak aktif", HttpStatus.FORBIDDEN);
        }

        if (user.getRole() != Role.UMKM) {
            throw new AppException(
                    "Akses hanya untuk akun UMKM", HttpStatus.FORBIDDEN);
        }

        UmkmProfile profile = umkmProfileRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new AppException(
                        "Profil usaha tidak ditemukan",
                        HttpStatus.NOT_FOUND));

        if (!profile.isActive()) {
            throw new AppException(
                    "Profil usaha sudah tidak aktif",
                    HttpStatus.FORBIDDEN);
        }

        profile.setBusinessName(request.getBusinessName());
        profile.setCategory(request.getCategory());
        profile.setDescription(request.getDescription());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setContactEmail(request.getContactEmail());

        if (request.getPassword() != null
                && !request.getPassword().isBlank()) {
            user.setPasswordHash(
                    passwordEncoder.encode(request.getPassword()));
            userRepository.save(user);
        }

        umkmProfileRepository.save(profile);
    }

    // DEACTIVATE MY ACCOUNT
    @Transactional
    public void deactivateMyAccount(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(
                        "Akun tidak ditemukan", HttpStatus.NOT_FOUND));

        if (!user.isActive()) {
            throw new AppException(
                    "Akun sudah tidak aktif", HttpStatus.FORBIDDEN);
        }

        if (user.getRole() != Role.UMKM) {
            throw new AppException(
                    "Akses hanya untuk akun UMKM", HttpStatus.FORBIDDEN);
        }

        user.setActive(false);
        userRepository.save(user);
    }
}
