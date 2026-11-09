package com.jemari.catalog.category;

import com.jemari.catalog.category.dto.CreateKategoriRequest;
import com.jemari.catalog.category.dto.KategoriResponse;
import com.jemari.catalog.category.dto.KategoriTreeNodeResponse;
import com.jemari.catalog.category.dto.UpdateKategoriRequest;
import com.jemari.catalog.listing.ServicePackageRepository;
import com.jemari.common.exception.AppException;
import com.jemari.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class KategoriServiceTest {

    @Mock
    private ServiceCategoryRepository serviceCategoryRepository;

    @Mock
    private ServicePackageRepository servicePackageRepository;

    @InjectMocks
    private KategoriService kategoriService;

    private ServiceCategory rootKategori;
    private ServiceCategory subKategori;

    @BeforeEach
    void setUp() {
        rootKategori = new ServiceCategory("Desain Grafis", "Kategori desain grafis", null);
        rootKategori.setId(1L);

        subKategori = new ServiceCategory("Desain Logo", "Sub kategori desain logo", rootKategori);
        subKategori.setId(2L);
        rootKategori.setSubCategories(List.of(subKategori));
    }

    @Test
    @DisplayName("Admin dapat menambah kategori root baru")
    void createKategori_Root_Success() {
        CreateKategoriRequest request = new CreateKategoriRequest("Web Development", "Pembuatan web", null);

        when(serviceCategoryRepository.existsByNameIgnoreCaseAndParentIsNull("Web Development")).thenReturn(false);
        when(serviceCategoryRepository.save(any(ServiceCategory.class))).thenAnswer(invocation -> {
            ServiceCategory entity = invocation.getArgument(0);
            entity.setId(10L);
            return entity;
        });

        KategoriResponse response = kategoriService.createKategori(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.nama()).isEqualTo("Web Development");
        assertThat(response.parentId()).isNull();
        verify(serviceCategoryRepository).save(any(ServiceCategory.class));
    }

    @Test
    @DisplayName("Admin dapat menambah sub-kategori baru")
    void createKategori_SubKategori_Success() {
        CreateKategoriRequest request = new CreateKategoriRequest("Frontend", "Frontend web", 1L);

        when(serviceCategoryRepository.findById(1L)).thenReturn(Optional.of(rootKategori));
        when(serviceCategoryRepository.existsByNameIgnoreCaseAndParent("Frontend", rootKategori)).thenReturn(false);
        when(serviceCategoryRepository.save(any(ServiceCategory.class))).thenAnswer(invocation -> {
            ServiceCategory entity = invocation.getArgument(0);
            entity.setId(11L);
            return entity;
        });

        KategoriResponse response = kategoriService.createKategori(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(11L);
        assertThat(response.nama()).isEqualTo("Frontend");
        assertThat(response.parentId()).isEqualTo(1L);
        verify(serviceCategoryRepository).save(any(ServiceCategory.class));
    }

    @Test
    @DisplayName("Tambah kategori root ditolak jika nama duplikat (case-insensitive)")
    void createKategori_DuplicateRoot_ThrowsConflict() {
        CreateKategoriRequest request = new CreateKategoriRequest("desain grafis", "Deskripsi", null);

        when(serviceCategoryRepository.existsByNameIgnoreCaseAndParentIsNull("desain grafis")).thenReturn(true);

        assertThatThrownBy(() -> kategoriService.createKategori(request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("sudah digunakan")
                .matches(ex -> ((AppException) ex).getStatus() == HttpStatus.CONFLICT);

        verify(serviceCategoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tambah sub-kategori ditolak jika nama duplikat dalam parent yang sama")
    void createKategori_DuplicateSub_ThrowsConflict() {
        CreateKategoriRequest request = new CreateKategoriRequest("DESAIN LOGO", "Deskripsi", 1L);

        when(serviceCategoryRepository.findById(1L)).thenReturn(Optional.of(rootKategori));
        when(serviceCategoryRepository.existsByNameIgnoreCaseAndParent("DESAIN LOGO", rootKategori)).thenReturn(true);

        assertThatThrownBy(() -> kategoriService.createKategori(request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("sudah digunakan pada kategori induk ini")
                .matches(ex -> ((AppException) ex).getStatus() == HttpStatus.CONFLICT);

        verify(serviceCategoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tambah sub-kategori ditolak jika parentId tidak ditemukan")
    void createKategori_ParentNotFound_ThrowsNotFound() {
        CreateKategoriRequest request = new CreateKategoriRequest("Sub Kat", "Deskripsi", 999L);

        when(serviceCategoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kategoriService.createKategori(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Kategori induk tidak ditemukan");
    }

    @Test
    @DisplayName("Ubah kategori ditolak jika menjadikan kategori sebagai parent diri sendiri")
    void updateKategori_SelfParent_ThrowsBadRequest() {
        UpdateKategoriRequest request = new UpdateKategoriRequest("Desain Grafis", "Deskripsi baru", 1L, true);

        when(serviceCategoryRepository.findById(1L)).thenReturn(Optional.of(rootKategori));

        assertThatThrownBy(() -> kategoriService.updateKategori(1L, request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("tidak dapat menjadi sub-kategori dari dirinya sendiri")
                .matches(ex -> ((AppException) ex).getStatus() == HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Hapus kategori ditolak jika masih memiliki sub-kategori")
    void deleteKategori_HasChildren_ThrowsBadRequest() {
        when(serviceCategoryRepository.findById(1L)).thenReturn(Optional.of(rootKategori));
        when(serviceCategoryRepository.existsByParentId(1L)).thenReturn(true);

        assertThatThrownBy(() -> kategoriService.deleteKategori(1L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("masih memiliki sub-kategori")
                .matches(ex -> ((AppException) ex).getStatus() == HttpStatus.BAD_REQUEST);

        verify(serviceCategoryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Hapus kategori ditolak jika masih digunakan oleh paket jasa")
    void deleteKategori_UsedByPaketJasa_ThrowsBadRequest() {
        when(serviceCategoryRepository.findById(2L)).thenReturn(Optional.of(subKategori));
        when(serviceCategoryRepository.existsByParentId(2L)).thenReturn(false);
        when(servicePackageRepository.countByCategoryId(2L)).thenReturn(1L);

        assertThatThrownBy(() -> kategoriService.deleteKategori(2L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("masih digunakan oleh paket jasa")
                .matches(ex -> ((AppException) ex).getStatus() == HttpStatus.BAD_REQUEST);

        verify(serviceCategoryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Hapus kategori berhasil jika tidak memiliki anak dan tidak digunakan paket jasa")
    void deleteKategori_Success() {
        when(serviceCategoryRepository.findById(2L)).thenReturn(Optional.of(subKategori));
        when(serviceCategoryRepository.existsByParentId(2L)).thenReturn(false);
        when(servicePackageRepository.countByCategoryId(2L)).thenReturn(0L);

        kategoriService.deleteKategori(2L);

        verify(serviceCategoryRepository).delete(subKategori);
    }

    @Test
    @DisplayName("Daftar pohon kategori aktif hanya mengembalikan kategori yang aktif")
    void getActiveKategoriTree_ReturnsActiveOnly() {
        ServiceCategory inactiveSub = new ServiceCategory("Sub Nonaktif", null, rootKategori);
        inactiveSub.setId(3L);
        inactiveSub.setActive(false);

        rootKategori.setSubCategories(List.of(subKategori, inactiveSub));

        when(serviceCategoryRepository.findAllByActiveTrueAndParentIsNull()).thenReturn(List.of(rootKategori));

        List<KategoriTreeNodeResponse> tree = kategoriService.getActiveKategoriTree();

        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).nama()).isEqualTo("Desain Grafis");
        assertThat(tree.get(0).subKategori()).hasSize(1);
        assertThat(tree.get(0).subKategori().get(0).nama()).isEqualTo("Desain Logo");
    }
}
