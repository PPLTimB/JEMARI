package com.jemari.catalog.variant.controller;

import com.jemari.catalog.variant.dto.CreateVariantRequest;
import com.jemari.catalog.variant.dto.UpdateVariantRequest;
import com.jemari.catalog.variant.dto.VariantResponse;
import com.jemari.catalog.variant.service.ServicePackageVariantService;
import com.jemari.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller untuk pengelolaan varian paket jasa oleh Freelancer (UC-18, FR-02).
 * Hanya dapat diakses oleh user dengan role FREELANCER untuk paket miliknya sendiri.
 */
@RestController
@RequestMapping("/api/freelancer/paket/{paketId}/varian")
@PreAuthorize("hasRole('FREELANCER')")
public class FreelancerPackageVariantController {

    private final ServicePackageVariantService variantService;

    public FreelancerPackageVariantController(ServicePackageVariantService variantService) {
        this.variantService = variantService;
    }

    /**
     * Mengambil daftar varian aktif pada paket tertentu milik Freelancer.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<VariantResponse>>> getVariants(
            @PathVariable Long paketId,
            Authentication authentication) {
        String email = authentication.getName();
        List<VariantResponse> variants = variantService.getVariantsForFreelancer(paketId, email);
        return ResponseEntity.ok(ApiResponse.ok("Berhasil mengambil daftar varian paket", variants));
    }

    /**
     * Menambahkan varian harga dan estimasi durasi baru pada paket jasa.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<VariantResponse>> createVariant(
            @PathVariable Long paketId,
            @Valid @RequestBody CreateVariantRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        VariantResponse created = variantService.createVariant(paketId, request, email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Berhasil menambahkan varian paket", created));
    }

    /**
     * Memperbarui varian harga dan estimasi durasi pada paket jasa.
     */
    @PutMapping("/{varianId}")
    public ResponseEntity<ApiResponse<VariantResponse>> updateVariant(
            @PathVariable Long paketId,
            @PathVariable Long varianId,
            @Valid @RequestBody UpdateVariantRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        VariantResponse updated = variantService.updateVariant(paketId, varianId, request, email);
        return ResponseEntity.ok(ApiResponse.ok("Berhasil memperbarui varian paket", updated));
    }

    /**
     * Menghapus varian paket jasa secara soft-delete.
     */
    @DeleteMapping("/{varianId}")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(
            @PathVariable Long paketId,
            @PathVariable Long varianId,
            Authentication authentication) {
        String email = authentication.getName();
        variantService.deleteVariant(paketId, varianId, email);
        return ResponseEntity.ok(ApiResponse.ok("Berhasil menghapus varian paket", null));
    }
}
