package com.jemari.catalog.variant.service;

import com.jemari.catalog.variant.checker.VariantUsageChecker;
import com.jemari.catalog.variant.dto.CreateVariantRequest;
import com.jemari.catalog.variant.dto.EstimasiDurasiResponse;
import com.jemari.catalog.variant.dto.RincianBiayaResponse;
import com.jemari.catalog.variant.dto.UpdateVariantRequest;
import com.jemari.catalog.variant.dto.VariantItemResponse;
import com.jemari.catalog.variant.dto.VariantResponse;
import com.jemari.catalog.variant.entity.ServicePackageVariant;
import com.jemari.catalog.variant.lookup.PackageLookup;
import com.jemari.catalog.variant.repository.ServicePackageVariantRepository;
import com.jemari.common.exception.AppException;
import com.jemari.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Service untuk mengelola varian harga dan estimasi durasi paket jasa.
 * Menangani logika bisnis PBI-04 (UC-03, UC-18, FR-02).
 */
@Service
public class ServicePackageVariantService {

    private static final Logger log = LoggerFactory.getLogger(ServicePackageVariantService.class);

    private final ServicePackageVariantRepository variantRepository;
    private final PackageLookup packageLookup;
    private final VariantUsageChecker variantUsageChecker;

    public ServicePackageVariantService(
            ServicePackageVariantRepository variantRepository,
            PackageLookup packageLookup,
            VariantUsageChecker variantUsageChecker) {
        this.variantRepository = variantRepository;
        this.packageLookup = packageLookup;
        this.variantUsageChecker = variantUsageChecker;
    }

    /**
     * Mengambil daftar seluruh varian aktif dari paket milik freelancer.
     */
    @Transactional(readOnly = true)
    public List<VariantResponse> getVariantsForFreelancer(Long packageId, String freelancerEmail) {
        validateFreelancerPackageOwnership(packageId, freelancerEmail);

        return variantRepository.findByPackageIdAndActiveTrueOrderByPriceAsc(packageId)
                .stream()
                .map(VariantResponse::fromEntity)
                .toList();
    }

    /**
     * Menambahkan varian baru pada paket milik freelancer.
     */
    @Transactional
    public VariantResponse createVariant(Long packageId, CreateVariantRequest request, String freelancerEmail) {
        validateFreelancerPackageOwnership(packageId, freelancerEmail);

        String trimmedName = request.name().trim();
        if (variantRepository.existsByPackageIdAndNameIgnoreCaseAndActiveTrue(packageId, trimmedName)) {
            throw new AppException(
                    "Nama varian '" + trimmedName + "' sudah digunakan pada paket ini",
                    HttpStatus.BAD_REQUEST
            );
        }

        String description = request.description() != null ? request.description().trim() : null;
        ServicePackageVariant variant = new ServicePackageVariant(
                packageId,
                trimmedName,
                description,
                request.price(),
                request.estimatedDays()
        );

        ServicePackageVariant savedVariant = variantRepository.save(variant);
        log.info("Varian baru '{}' berhasil ditambahkan pada paket ID {}", trimmedName, packageId);
        return VariantResponse.fromEntity(savedVariant);
    }

    /**
     * Memperbarui data varian pada paket milik freelancer.
     * Sesuai UC-18: Perubahan harga atau durasi hanya berlaku untuk pemesanan baru,
     * sedangkan pesanan yang sudah berjalan tetap memakai ketentuan awal.
     */
    @Transactional
    public VariantResponse updateVariant(Long packageId, Long variantId, UpdateVariantRequest request, String freelancerEmail) {
        validateFreelancerPackageOwnership(packageId, freelancerEmail);

        ServicePackageVariant variant = variantRepository.findByIdAndPackageIdAndActiveTrue(variantId, packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Varian tidak ditemukan pada paket ini"));

        // Pemeriksaan apakah varian sedang digunakan dalam pesanan aktif
        boolean isInActiveOrder = variantUsageChecker.isVariantInActiveOrder(variantId);
        if (isInActiveOrder) {
            log.info("Varian ID {} sedang digunakan dalam pesanan aktif. Perubahan harga/durasi hanya berlaku untuk pemesanan baru.", variantId);
        }

        String trimmedName = request.name().trim();
        if (variantRepository.existsByPackageIdAndNameIgnoreCaseAndActiveTrueAndIdNot(packageId, trimmedName, variantId)) {
            throw new AppException(
                    "Nama varian '" + trimmedName + "' sudah digunakan pada paket ini",
                    HttpStatus.BAD_REQUEST
            );
        }

        variant.setName(trimmedName);
        variant.setDescription(request.description() != null ? request.description().trim() : null);
        variant.setPrice(request.price());
        variant.setEstimatedDays(request.estimatedDays());

        ServicePackageVariant updatedVariant = variantRepository.save(variant);
        log.info("Varian ID {} berhasil diperbarui pada paket ID {}", variantId, packageId);
        return VariantResponse.fromEntity(updatedVariant);
    }

    /**
     * Menghapus varian dari paket secara soft-delete (is_active = false).
     */
    @Transactional
    public void deleteVariant(Long packageId, Long variantId, String freelancerEmail) {
        validateFreelancerPackageOwnership(packageId, freelancerEmail);

        ServicePackageVariant variant = variantRepository.findByIdAndPackageIdAndActiveTrue(variantId, packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Varian tidak ditemukan pada paket ini"));

        boolean isInActiveOrder = variantUsageChecker.isVariantInActiveOrder(variantId);
        if (isInActiveOrder) {
            log.info("Menonaktifkan varian ID {} yang terikat pesanan berjalan. Pesanan berjalan tetap memakai ketentuan awal.", variantId);
        }

        variant.setActive(false);
        variantRepository.save(variant);
        log.info("Varian ID {} berhasil dinonaktifkan (soft delete) dari paket ID {}", variantId, packageId);
    }

    /**
     * Mengambil rincian biaya paket untuk sisi UMKM (UC-03, FR-02).
     * Jika paket tidak ada atau tidak aktif, menampilkan pesan 'Paket tidak tersedia' (404).
     */
    @Transactional(readOnly = true)
    public RincianBiayaResponse getRincianBiayaForUmkm(Long packageId) {
        if (!packageLookup.packageExists(packageId) || !packageLookup.isPackageActive(packageId)) {
            throw new ResourceNotFoundException("Paket tidak tersedia");
        }

        List<ServicePackageVariant> variants = variantRepository.findByPackageIdAndActiveTrueOrderByPriceAsc(packageId);

        if (variants.isEmpty()) {
            return new RincianBiayaResponse(packageId, null, null, Collections.emptyList());
        }

        BigDecimal hargaMulai = variants.getFirst().getPrice();
        int durasiTercepat = variants.stream().mapToInt(ServicePackageVariant::getEstimatedDays).min().orElse(0);
        int durasiTerlama = variants.stream().mapToInt(ServicePackageVariant::getEstimatedDays).max().orElse(0);

        EstimasiDurasiResponse ringkasanEstimasi = new EstimasiDurasiResponse(durasiTercepat, durasiTerlama);
        List<VariantItemResponse> variantItems = variants.stream()
                .map(VariantItemResponse::fromEntity)
                .toList();

        return new RincianBiayaResponse(packageId, hargaMulai, ringkasanEstimasi, variantItems);
    }

    /**
     * Validasi keberadaan paket, status aktif, dan kepemilikan oleh freelancer.
     */
    private void validateFreelancerPackageOwnership(Long packageId, String freelancerEmail) {
        if (!packageLookup.packageExists(packageId) || !packageLookup.isPackageActive(packageId)) {
            throw new ResourceNotFoundException("Paket tidak tersedia");
        }

        if (!packageLookup.isPackageOwnedByFreelancer(packageId, freelancerEmail)) {
            throw new AppException("Anda tidak memiliki akses untuk mengelola paket ini", HttpStatus.FORBIDDEN);
        }
    }
}
