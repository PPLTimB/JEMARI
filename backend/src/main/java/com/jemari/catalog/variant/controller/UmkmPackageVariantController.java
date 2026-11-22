package com.jemari.catalog.variant.controller;

import com.jemari.catalog.variant.dto.RincianBiayaResponse;
import com.jemari.catalog.variant.service.ServicePackageVariantService;
import com.jemari.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller untuk melihat rincian biaya paket jasa oleh UMKM (UC-03, FR-02).
 * Menampilkan varian aktif, harga mulai dari, dan ringkasan estimasi pengerjaan.
 */
@RestController
@RequestMapping("/api/paket/{paketId}/rincian-biaya")
@PreAuthorize("hasRole('UMKM')")
public class UmkmPackageVariantController {

    private final ServicePackageVariantService variantService;

    public UmkmPackageVariantController(ServicePackageVariantService variantService) {
        this.variantService = variantService;
    }

    /**
     * Mengambil rincian biaya paket jasa untuk UMKM.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<RincianBiayaResponse>> getRincianBiaya(
            @PathVariable Long paketId) {
        RincianBiayaResponse rincianBiaya = variantService.getRincianBiayaForUmkm(paketId);
        return ResponseEntity.ok(ApiResponse.ok("Berhasil mengambil rincian biaya paket", rincianBiaya));
    }
}
