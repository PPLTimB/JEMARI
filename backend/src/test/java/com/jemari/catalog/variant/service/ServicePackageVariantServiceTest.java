package com.jemari.catalog.variant.service;

import com.jemari.catalog.variant.checker.VariantUsageChecker;
import com.jemari.catalog.variant.dto.CreateVariantRequest;
import com.jemari.catalog.variant.dto.RincianBiayaResponse;
import com.jemari.catalog.variant.dto.UpdateVariantRequest;
import com.jemari.catalog.variant.dto.VariantResponse;
import com.jemari.catalog.variant.entity.ServicePackageVariant;
import com.jemari.catalog.variant.lookup.PackageLookup;
import com.jemari.catalog.variant.repository.ServicePackageVariantRepository;
import com.jemari.common.exception.AppException;
import com.jemari.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicePackageVariantServiceTest {

    @Mock
    private ServicePackageVariantRepository variantRepository;

    @Mock
    private PackageLookup packageLookup;

    @Mock
    private VariantUsageChecker variantUsageChecker;

    @InjectMocks
    private ServicePackageVariantService variantService;

    private final Long packageId = 100L;
    private final String freelancerEmail = "freelancer@jemari.com";

    @Nested
    @DisplayName("Pengujian getVariantsForFreelancer")
    class GetVariantsTests {

        @Test
        @DisplayName("Berhasil mengambil daftar varian aktif paket milik freelancer")
        void getVariants_Success() {
            when(packageLookup.packageExists(packageId)).thenReturn(true);
            when(packageLookup.isPackageActive(packageId)).thenReturn(true);
            when(packageLookup.isPackageOwnedByFreelancer(packageId, freelancerEmail)).thenReturn(true);

            ServicePackageVariant v1 = new ServicePackageVariant(packageId, "Basic", "Desain 1 konsep", new BigDecimal("50000.00"), 2);
            ServicePackageVariant v2 = new ServicePackageVariant(packageId, "Premium", "Desain 3 konsep", new BigDecimal("150000.00"), 5);
            when(variantRepository.findByPackageIdAndActiveTrueOrderByPriceAsc(packageId)).thenReturn(List.of(v1, v2));

            List<VariantResponse> result = variantService.getVariantsForFreelancer(packageId, freelancerEmail);

            assertEquals(2, result.size());
            assertEquals("Basic", result.get(0).name());
            assertEquals("Premium", result.get(1).name());
        }

        @Test
        @DisplayName("Gagal ketika paket tidak ditemukan atau tidak aktif (404)")
        void getVariants_PackageNotFoundOrInactive_ThrowsNotFound() {
            when(packageLookup.packageExists(packageId)).thenReturn(false);

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> variantService.getVariantsForFreelancer(packageId, freelancerEmail));

            assertEquals("Paket tidak tersedia", ex.getMessage());
        }

        @Test
        @DisplayName("Gagal ketika paket bukan milik freelancer yang login (403)")
        void getVariants_NotOwner_ThrowsForbidden() {
            when(packageLookup.packageExists(packageId)).thenReturn(true);
            when(packageLookup.isPackageActive(packageId)).thenReturn(true);
            when(packageLookup.isPackageOwnedByFreelancer(packageId, freelancerEmail)).thenReturn(false);

            AppException ex = assertThrows(AppException.class,
                    () -> variantService.getVariantsForFreelancer(packageId, freelancerEmail));

            assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
            assertEquals("Anda tidak memiliki akses untuk mengelola paket ini", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Pengujian createVariant")
    class CreateVariantTests {

        private final CreateVariantRequest validRequest = new CreateVariantRequest(
                "Standard",
                "Paket standar lengkap",
                new BigDecimal("100000.00"),
                3
        );

        @Test
        @DisplayName("Berhasil menambahkan varian baru")
        void createVariant_Success() {
            when(packageLookup.packageExists(packageId)).thenReturn(true);
            when(packageLookup.isPackageActive(packageId)).thenReturn(true);
            when(packageLookup.isPackageOwnedByFreelancer(packageId, freelancerEmail)).thenReturn(true);
            when(variantRepository.existsByPackageIdAndNameIgnoreCaseAndActiveTrue(packageId, "Standard")).thenReturn(false);

            ServicePackageVariant saved = new ServicePackageVariant(packageId, "Standard", "Paket standar lengkap", new BigDecimal("100000.00"), 3);
            when(variantRepository.save(any(ServicePackageVariant.class))).thenReturn(saved);

            VariantResponse response = variantService.createVariant(packageId, validRequest, freelancerEmail);

            assertNotNull(response);
            assertEquals("Standard", response.name());
            assertEquals(new BigDecimal("100000.00"), response.price());
            assertEquals(3, response.estimatedDays());
            assertTrue(response.active());
        }

        @Test
        @DisplayName("Gagal menambahkan varian dengan nama kembar (case-insensitive) dalam paket yang sama (400)")
        void createVariant_DuplicateName_ThrowsBadRequest() {
            when(packageLookup.packageExists(packageId)).thenReturn(true);
            when(packageLookup.isPackageActive(packageId)).thenReturn(true);
            when(packageLookup.isPackageOwnedByFreelancer(packageId, freelancerEmail)).thenReturn(true);
            when(variantRepository.existsByPackageIdAndNameIgnoreCaseAndActiveTrue(packageId, "Standard")).thenReturn(true);

            AppException ex = assertThrows(AppException.class,
                    () -> variantService.createVariant(packageId, validRequest, freelancerEmail));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
            assertTrue(ex.getMessage().contains("sudah digunakan pada paket ini"));
            verify(variantRepository, never()).save(any());
        }

        @Test
        @DisplayName("Gagal menambahkan varian pada paket orang lain (403)")
        void createVariant_NotOwner_ThrowsForbidden() {
            when(packageLookup.packageExists(packageId)).thenReturn(true);
            when(packageLookup.isPackageActive(packageId)).thenReturn(true);
            when(packageLookup.isPackageOwnedByFreelancer(packageId, freelancerEmail)).thenReturn(false);

            AppException ex = assertThrows(AppException.class,
                    () -> variantService.createVariant(packageId, validRequest, freelancerEmail));

            assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        }
    }

    @Nested
    @DisplayName("Pengujian updateVariant")
    class UpdateVariantTests {

        private final Long variantId = 1L;
        private final UpdateVariantRequest updateRequest = new UpdateVariantRequest(
                "Super Premium",
                "Deskripsi baru",
                new BigDecimal("250000.00"),
                7
        );

        @Test
        @DisplayName("Berhasil memperbarui varian paket")
        void updateVariant_Success() {
            when(packageLookup.packageExists(packageId)).thenReturn(true);
            when(packageLookup.isPackageActive(packageId)).thenReturn(true);
            when(packageLookup.isPackageOwnedByFreelancer(packageId, freelancerEmail)).thenReturn(true);

            ServicePackageVariant existing = new ServicePackageVariant(packageId, "Premium", "Deskripsi lama", new BigDecimal("200000.00"), 5);
            when(variantRepository.findByIdAndPackageIdAndActiveTrue(variantId, packageId)).thenReturn(Optional.of(existing));
            when(variantUsageChecker.isVariantInActiveOrder(variantId)).thenReturn(false);
            when(variantRepository.existsByPackageIdAndNameIgnoreCaseAndActiveTrueAndIdNot(packageId, "Super Premium", variantId)).thenReturn(false);
            when(variantRepository.save(existing)).thenReturn(existing);

            VariantResponse response = variantService.updateVariant(packageId, variantId, updateRequest, freelancerEmail);

            assertEquals("Super Premium", response.name());
            assertEquals("Deskripsi baru", response.description());
            assertEquals(new BigDecimal("250000.00"), response.price());
            assertEquals(7, response.estimatedDays());
        }

        @Test
        @DisplayName("Gagal memperbarui jika varian tidak ditemukan atau sudah nonaktif (404)")
        void updateVariant_VariantNotFound_ThrowsNotFound() {
            when(packageLookup.packageExists(packageId)).thenReturn(true);
            when(packageLookup.isPackageActive(packageId)).thenReturn(true);
            when(packageLookup.isPackageOwnedByFreelancer(packageId, freelancerEmail)).thenReturn(true);
            when(variantRepository.findByIdAndPackageIdAndActiveTrue(variantId, packageId)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> variantService.updateVariant(packageId, variantId, updateRequest, freelancerEmail));

            assertEquals("Varian tidak ditemukan pada paket ini", ex.getMessage());
        }

        @Test
        @DisplayName("Gagal memperbarui jika nama varian kembar dengan varian lain di paket yang sama (400)")
        void updateVariant_DuplicateName_ThrowsBadRequest() {
            when(packageLookup.packageExists(packageId)).thenReturn(true);
            when(packageLookup.isPackageActive(packageId)).thenReturn(true);
            when(packageLookup.isPackageOwnedByFreelancer(packageId, freelancerEmail)).thenReturn(true);

            ServicePackageVariant existing = new ServicePackageVariant(packageId, "Premium", "Deskripsi", new BigDecimal("200000.00"), 5);
            when(variantRepository.findByIdAndPackageIdAndActiveTrue(variantId, packageId)).thenReturn(Optional.of(existing));
            when(variantRepository.existsByPackageIdAndNameIgnoreCaseAndActiveTrueAndIdNot(packageId, "Super Premium", variantId)).thenReturn(true);

            AppException ex = assertThrows(AppException.class,
                    () -> variantService.updateVariant(packageId, variantId, updateRequest, freelancerEmail));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
            assertTrue(ex.getMessage().contains("sudah digunakan pada paket ini"));
        }
    }

    @Nested
    @DisplayName("Pengujian deleteVariant")
    class DeleteVariantTests {

        private final Long variantId = 1L;

        @Test
        @DisplayName("Berhasil melakukan soft-delete varian")
        void deleteVariant_Success() {
            when(packageLookup.packageExists(packageId)).thenReturn(true);
            when(packageLookup.isPackageActive(packageId)).thenReturn(true);
            when(packageLookup.isPackageOwnedByFreelancer(packageId, freelancerEmail)).thenReturn(true);

            ServicePackageVariant existing = new ServicePackageVariant(packageId, "Basic", "Desc", new BigDecimal("50000.00"), 2);
            assertTrue(existing.isActive());
            when(variantRepository.findByIdAndPackageIdAndActiveTrue(variantId, packageId)).thenReturn(Optional.of(existing));
            when(variantUsageChecker.isVariantInActiveOrder(variantId)).thenReturn(false);

            variantService.deleteVariant(packageId, variantId, freelancerEmail);

            assertFalse(existing.isActive());
            verify(variantRepository).save(existing);
        }

        @Test
        @DisplayName("Gagal menghapus jika varian tidak ditemukan (404)")
        void deleteVariant_NotFound_ThrowsNotFound() {
            when(packageLookup.packageExists(packageId)).thenReturn(true);
            when(packageLookup.isPackageActive(packageId)).thenReturn(true);
            when(packageLookup.isPackageOwnedByFreelancer(packageId, freelancerEmail)).thenReturn(true);
            when(variantRepository.findByIdAndPackageIdAndActiveTrue(variantId, packageId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> variantService.deleteVariant(packageId, variantId, freelancerEmail));
        }
    }

    @Nested
    @DisplayName("Pengujian getRincianBiayaForUmkm (UC-03, FR-02)")
    class GetRincianBiayaTests {

        @Test
        @DisplayName("Berhasil mendapatkan rincian biaya lengkap dengan varian, hargaMulai, dan ringkasan estimasi")
        void getRincianBiaya_Success() {
            when(packageLookup.packageExists(packageId)).thenReturn(true);
            when(packageLookup.isPackageActive(packageId)).thenReturn(true);

            ServicePackageVariant v1 = new ServicePackageVariant(packageId, "Paket Kilat", "1 Hari Jadi", new BigDecimal("75000.00"), 1);
            ServicePackageVariant v2 = new ServicePackageVariant(packageId, "Paket Reguler", "3 Hari Jadi", new BigDecimal("50000.00"), 3);
            ServicePackageVariant v3 = new ServicePackageVariant(packageId, "Paket Komplit", "7 Hari Jadi", new BigDecimal("120000.00"), 7);

            // Repository findByPackageIdAndActiveTrueOrderByPriceAsc mengembalikan urut harga termurah: v2 (50000), v1 (75000), v3 (120000)
            when(variantRepository.findByPackageIdAndActiveTrueOrderByPriceAsc(packageId))
                    .thenReturn(List.of(v2, v1, v3));

            RincianBiayaResponse response = variantService.getRincianBiayaForUmkm(packageId);

            assertNotNull(response);
            assertEquals(packageId, response.packageId());
            assertEquals(new BigDecimal("50000.00"), response.hargaMulai(), "Harga mulai harus berupa harga termurah");
            assertNotNull(response.ringkasanEstimasi());
            assertEquals(1, response.ringkasanEstimasi().durasiTercepat(), "Durasi tercepat adalah 1 hari");
            assertEquals(7, response.ringkasanEstimasi().durasiTerlama(), "Durasi terlama adalah 7 hari");
            assertEquals(3, response.varian().size());
        }

        @Test
        @DisplayName("Berhasil mendapatkan rincian ketika paket belum memiliki varian aktif")
        void getRincianBiaya_NoVariants() {
            when(packageLookup.packageExists(packageId)).thenReturn(true);
            when(packageLookup.isPackageActive(packageId)).thenReturn(true);
            when(variantRepository.findByPackageIdAndActiveTrueOrderByPriceAsc(packageId))
                    .thenReturn(Collections.emptyList());

            RincianBiayaResponse response = variantService.getRincianBiayaForUmkm(packageId);

            assertNotNull(response);
            assertEquals(packageId, response.packageId());
            assertNull(response.hargaMulai());
            assertNull(response.ringkasanEstimasi());
            assertTrue(response.varian().isEmpty());
        }

        @Test
        @DisplayName("Gagal dan menampilkan 'Paket tidak tersedia' (404) jika paket tidak ada atau tidak aktif (UC-03)")
        void getRincianBiaya_PackageInactiveOrNotFound_ThrowsNotFound() {
            when(packageLookup.packageExists(packageId)).thenReturn(true);
            when(packageLookup.isPackageActive(packageId)).thenReturn(false);

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> variantService.getRincianBiayaForUmkm(packageId));

            assertEquals("Paket tidak tersedia", ex.getMessage());
        }
    }
}
