package com.jemari.servicepackage.controller;

import com.jemari.servicepackage.dto.ServicePackageRequest;
import com.jemari.servicepackage.dto.ServicePackageResponse;
import com.jemari.servicepackage.service.ServicePackageService;
import com.jemari.user.User;
import com.jemari.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-packages")
public class ServicePackageController {

    private final ServicePackageService servicePackageService;
    private final UserRepository userRepository;

    public ServicePackageController(ServicePackageService servicePackageService,
                                    UserRepository userRepository) {
        this.servicePackageService = servicePackageService;
        this.userRepository = userRepository;
    }

    private Long getCurrentFreelancerId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Pengguna belum terautentikasi");
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Pengguna tidak ditemukan"));

        if (!"FREELANCER".equals(user.getRole().name())) {
            throw new RuntimeException("Hanya FREELANCER yang dapat mengelola paket jasa");
        }
        return user.getId();
    }

    @PostMapping
    public ResponseEntity<ServicePackageResponse> createPackage(
            Authentication authentication,
            @Valid @RequestBody ServicePackageRequest request) {
        Long freelancerId = getCurrentFreelancerId(authentication);
        ServicePackageResponse response = servicePackageService.createPackage(freelancerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ServicePackageResponse>> getAllActivePackages() {
        return ResponseEntity.ok(servicePackageService.getAllActivePackages());
    }

    @GetMapping("/me")
    public ResponseEntity<List<ServicePackageResponse>> getMyPackages(Authentication authentication) {
        Long freelancerId = getCurrentFreelancerId(authentication);
        return ResponseEntity.ok(servicePackageService.getMyPackages(freelancerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicePackageResponse> updatePackage(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody ServicePackageRequest request) {
        Long freelancerId = getCurrentFreelancerId(authentication);
        ServicePackageResponse response = servicePackageService.updatePackage(id, freelancerId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeletePackage(
            @PathVariable Long id,
            Authentication authentication) {
        Long freelancerId = getCurrentFreelancerId(authentication);
        servicePackageService.softDeletePackage(id, freelancerId);
        return ResponseEntity.noContent().build();
    }
}