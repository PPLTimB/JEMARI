package com.jemari.catalog.listing;

import com.jemari.catalog.category.ServiceCategory;
import com.jemari.catalog.category.ServiceCategoryRepository;
import com.jemari.catalog.listing.dto.KatalogFilterParam;
import com.jemari.catalog.listing.dto.KatalogPagedResponse;
import com.jemari.catalog.listing.dto.PaketJasaItemResponse;
import com.jemari.common.dto.ApiResponse;
import com.jemari.common.exception.AppException;
import com.jemari.user.FreelancerProfile;
import com.jemari.user.Role;
import com.jemari.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KatalogServiceTest {

    @Mock
    private ServicePackageRepository servicePackageRepository;

    @Mock
    private ServiceCategoryRepository serviceCategoryRepository;

    @Mock
    private FreelancerStatsService freelancerStatsService;

    @InjectMocks
    private KatalogService katalogService;

    private ServiceCategory parentKategori;
    private ServiceCategory childKategori;
    private FreelancerProfile newFreelancer;
    private FreelancerProfile regularFreelancer;

    @BeforeEach
    void setUp() {
        parentKategori = new ServiceCategory("Teknologi", "Kategori Teknologi", null);
        parentKategori.setId(100L);

        childKategori = new ServiceCategory("Web App", "Sub Web App", parentKategori);
        childKategori.setId(101L);

        User userNew = new User();
        userNew.setEmail("freelancer.baru@jemari.com");
        userNew.setRole(Role.FREELANCER);
        userNew.setActive(true);

        newFreelancer = new FreelancerProfile(userNew, "Freelancer Baru");
        newFreelancer.setId(1L);
        newFreelancer.setBio("Bio lengkap freelancer pemula");
        newFreelancer.setHeadline("Fullstack Developer");
        newFreelancer.setCity("Bandung");

        User userReg = new User();
        userReg.setEmail("freelancer.senior@jemari.com");
        userReg.setRole(Role.FREELANCER);
        userReg.setActive(true);

        regularFreelancer = new FreelancerProfile(userReg, "Freelancer Senior");
        regularFreelancer.setId(2L);
        regularFreelancer.setBio("Bio senior");
        regularFreelancer.setHeadline("Senior Engineer");
        regularFreelancer.setCity("Jakarta");
    }

    @Test
    @DisplayName("Validasi filter harga: tolak jika hargaMin lebih besar dari hargaMax")
    void searchKatalog_MinGreaterThanMax_ThrowsValidationException() {
        KatalogFilterParam param = new KatalogFilterParam(
                null,
                null,
                BigDecimal.valueOf(500000),
                BigDecimal.valueOf(100000),
                null,
                0,
                10
        );

        assertThatThrownBy(() -> katalogService.searchKatalog(param))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Validasi filter harga gagal")
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrors()).isNotNull();
                    assertThat(appEx.getErrors()).anyMatch(err ->
                            "hargaMin".equals(err.getField()) && err.getMessage().contains("tidak boleh lebih besar"));
                });
    }

    @Test
    @DisplayName("Validasi filter harga: tolak jika harga negatif")
    void searchKatalog_NegativePrice_ThrowsValidationException() {
        KatalogFilterParam param = new KatalogFilterParam(
                null,
                null,
                BigDecimal.valueOf(-1000),
                null,
                null,
                0,
                10
        );

        assertThatThrownBy(() -> katalogService.searchKatalog(param))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Validasi filter harga gagal")
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrors()).anyMatch(err ->
                            "hargaMin".equals(err.getField()) && err.getMessage().contains("tidak boleh bernilai negatif"));
                });
    }

    @Test
    @DisplayName("Jika tidak ada hasil, kembalikan response kosong dengan pesan saran melonggarkan filter")
    void searchKatalog_NoResults_ReturnsEmptyWithFriendlyMessage() {
        KatalogFilterParam param = new KatalogFilterParam(
                "TidakAdaKeyword",
                null,
                null,
                null,
                null,
                0,
                10
        );

        when(servicePackageRepository.count(any(Specification.class))).thenReturn(0L);

        ApiResponse<KatalogPagedResponse> response = katalogService.searchKatalog(param);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).containsIgnoringCase("jasa tidak ditemukan");
        assertThat(response.getMessage()).containsIgnoringCase("longgarkan filter");
        assertThat(response.getData().content()).isEmpty();
        assertThat(response.getData().totalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("Filter kategori induk secara otomatis mengikutsertakan sub-kategori aktifnya")
    void searchKatalog_ParentCategory_IncludesChildren() {
        KatalogFilterParam param = new KatalogFilterParam(
                null,
                100L,
                null,
                null,
                null,
                0,
                10
        );

        when(serviceCategoryRepository.findAllByParentId(100L)).thenReturn(List.of(childKategori));
        when(serviceCategoryRepository.findAllByParentId(101L)).thenReturn(List.of());
        when(servicePackageRepository.count(any(Specification.class))).thenReturn(0L);

        ApiResponse<KatalogPagedResponse> response = katalogService.searchKatalog(param);

        assertThat(response.isSuccess()).isTrue();
        verify(serviceCategoryRepository, atLeastOnce()).findAllByParentId(100L);
    }

    @Test
    @DisplayName("Aturan keadilan NFR-03: Posisi 1 dari setiap 5 dialokasikan untuk paket milik freelancer baru")
    void searchKatalog_FairnessRule_AllocatesSlotToNewFreelancer() {
        KatalogFilterParam param = new KatalogFilterParam(
                null,
                null,
                null,
                null,
                null,
                0,
                10
        );

        when(freelancerStatsService.getCompletedOrderCount(1L)).thenReturn(0);
        when(freelancerStatsService.getCompletedOrderCount(2L)).thenReturn(10); // Senior, order > 0

        ServicePackage pNew1 = createDummyPackage(10L, newFreelancer, parentKategori, "Paket Baru 1", BigDecimal.valueOf(100000));
        ServicePackage pNew2 = createDummyPackage(11L, newFreelancer, parentKategori, "Paket Baru 2", BigDecimal.valueOf(150000));

        List<ServicePackage> regularPackages = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            regularPackages.add(createDummyPackage(20L + i, regularFreelancer, parentKategori, "Paket Senior " + i, BigDecimal.valueOf(200000 + i * 10000)));
        }

        when(servicePackageRepository.count(any(Specification.class))).thenReturn(10L);

        when(servicePackageRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(pNew1, pNew2)))
                .thenReturn(new PageImpl<>(regularPackages));

        ApiResponse<KatalogPagedResponse> response = katalogService.searchKatalog(param);

        assertThat(response.isSuccess()).isTrue();
        List<PaketJasaItemResponse> content = response.getData().content();
        assertThat(content).hasSize(10);

        // Posisi index 0 (slot 1) harus paket dari freelancer baru
        assertThat(content.get(0).id()).isEqualTo(10L);
        assertThat(content.get(0).baru()).isTrue();

        // Posisi index 1 sampai 4 harus paket reguler
        for (int i = 1; i < 5; i++) {
            assertThat(content.get(i).baru()).isFalse();
        }

        // Posisi index 5 (slot ke-2 dari 5 posisi) harus paket dari freelancer baru
        assertThat(content.get(5).id()).isEqualTo(11L);
        assertThat(content.get(5).baru()).isTrue();

        // Posisi index 6 sampai 9 harus paket reguler
        for (int i = 6; i < 10; i++) {
            assertThat(content.get(i).baru()).isFalse();
        }
    }

    private ServicePackage createDummyPackage(Long id, FreelancerProfile fp, ServiceCategory kat, String title, BigDecimal basePrice) {
        ServicePackage pkg = new ServicePackage(fp, kat, title, "Deskripsi untuk " + title, basePrice);
        pkg.setId(id);
        return pkg;
    }
}
