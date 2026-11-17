package com.jemari.catalog.variant.repository;

import com.jemari.catalog.variant.entity.ServicePackageVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository data access untuk entitas ServicePackageVariant.
 */
@Repository
public interface ServicePackageVariantRepository extends JpaRepository<ServicePackageVariant, Long> {

    /**
     * Mengambil daftar varian aktif berdasarkan packageId, diurutkan dari harga termurah.
     */
    List<ServicePackageVariant> findByPackageIdAndActiveTrueOrderByPriceAsc(Long packageId);

    /**
     * Mengambil daftar varian aktif berdasarkan packageId.
     */
    List<ServicePackageVariant> findByPackageIdAndActiveTrue(Long packageId);

    /**
     * Mencari varian berdasarkan ID dan packageId (termasuk yang tidak aktif).
     */
    Optional<ServicePackageVariant> findByIdAndPackageId(Long id, Long packageId);

    /**
     * Mencari varian yang aktif berdasarkan ID dan packageId.
     */
    Optional<ServicePackageVariant> findByIdAndPackageIdAndActiveTrue(Long id, Long packageId);

    /**
     * Memeriksa apakah nama varian (case-insensitive) sudah digunakan pada paket yang sama dan masih aktif.
     */
    boolean existsByPackageIdAndNameIgnoreCaseAndActiveTrue(Long packageId, String name);

    /**
     * Memeriksa apakah nama varian (case-insensitive) sudah digunakan oleh varian aktif lain pada paket yang sama.
     */
    boolean existsByPackageIdAndNameIgnoreCaseAndActiveTrueAndIdNot(Long packageId, String name, Long id);
}
