package com.jemari.portfolio;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.jemari.common.exception.AppException;
import com.jemari.common.exception.ResourceNotFoundException;
import com.jemari.portfolio.dto.CreatePortfolioDto;
import com.jemari.portfolio.dto.PortfolioResponse;
import com.jemari.portfolio.dto.UpdatePortfolioDto;
import com.jemari.profile.FreelancerProfile;
import com.jemari.profile.FreelancerProfileRepository;
import com.jemari.user.Role;
import com.jemari.user.User;
import com.jemari.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private FreelancerProfileRepository freelancerProfileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DefaultPortfolioService portfolioService;

    private User freelancerUser;
    private User otherUser;
    private FreelancerProfile freelancerProfile;
    private Portfolio samplePortfolio;

    @BeforeEach
    void setUp() {
        freelancerUser = new User();
        freelancerUser.setId(10L);
        freelancerUser.setEmail("freelancer@jemari.com");
        freelancerUser.setRole(Role.FREELANCER);

        otherUser = new User();
        otherUser.setId(99L);
        otherUser.setEmail("other@jemari.com");
        otherUser.setRole(Role.FREELANCER);

        freelancerProfile = new FreelancerProfile();
        freelancerProfile.setId(1L);
        freelancerProfile.setUser(freelancerUser);
        freelancerProfile.setFullName("Budi Studio");

        samplePortfolio = new Portfolio(
                freelancerProfile,
                "Desain Identitas Brand & Logo",
                "Konsep logo modern minimalis",
                "https://images.unsplash.com/photo-1509440159596-0249088772ff.jpg"
        );
        samplePortfolio.setId(100L);
    }

    @Test
    @DisplayName("Tambah karya berhasil dengan data valid")
    void createPortfolio_success() {
        CreatePortfolioDto dto = new CreatePortfolioDto(
                "Desain Banner Media Sosial",
                "Banner promo IG",
                "https://example.com/banner.png"
        );

        when(freelancerProfileRepository.findByUserId(10L)).thenReturn(Optional.of(freelancerProfile));
        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(invocation -> {
            Portfolio p = invocation.getArgument(0);
            p.setId(101L);
            return p;
        });

        PortfolioResponse response = portfolioService.createPortfolio(10L, dto);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals("Desain Banner Media Sosial", response.getTitle());
        assertTrue(response.isActive());
        verify(portfolioRepository).save(any(Portfolio.class));
    }

    @Test
    @DisplayName("Tambah karya otomatis membuat profil jika user freelancer belum memiliki profil")
    void createPortfolio_autoCreateProfile() {
        CreatePortfolioDto dto = new CreatePortfolioDto(
                "Foto Produk Roti Artisan",
                "Foto katalog",
                "https://example.com/roti.jpg"
        );

        when(freelancerProfileRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(userRepository.findById(10L)).thenReturn(Optional.of(freelancerUser));
        when(freelancerProfileRepository.save(any(FreelancerProfile.class))).thenAnswer(inv -> {
            FreelancerProfile p = inv.getArgument(0);
            p.setId(2L);
            return p;
        });
        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(inv -> {
            Portfolio p = inv.getArgument(0);
            p.setId(102L);
            return p;
        });

        PortfolioResponse response = portfolioService.createPortfolio(10L, dto);

        assertNotNull(response);
        assertEquals(102L, response.getId());
        verify(freelancerProfileRepository).save(any(FreelancerProfile.class));
        verify(portfolioRepository).save(any(Portfolio.class));
    }

    @Test
    @DisplayName("Tambah karya gagal jika judul kosong")
    void createPortfolio_blankTitle_throwsException() {
        CreatePortfolioDto dto = new CreatePortfolioDto(
                "   ",
                "Deskripsi",
                "https://example.com/image.png"
        );

        AppException ex = assertThrows(AppException.class, () -> portfolioService.createPortfolio(10L, dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("Judul karya wajib diisi"));
        verify(portfolioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tambah karya gagal jika judul melebihi 150 karakter")
    void createPortfolio_titleTooLong_throwsException() {
        String longTitle = "A".repeat(151);
        CreatePortfolioDto dto = new CreatePortfolioDto(
                longTitle,
                "Deskripsi",
                "https://example.com/image.png"
        );

        AppException ex = assertThrows(AppException.class, () -> portfolioService.createPortfolio(10L, dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("150 karakter"));
        verify(portfolioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tambah karya gagal jika gambar kosong")
    void createPortfolio_blankImage_throwsException() {
        CreatePortfolioDto dto = new CreatePortfolioDto(
                "Judul Karya",
                "Deskripsi",
                "  "
        );

        AppException ex = assertThrows(AppException.class, () -> portfolioService.createPortfolio(10L, dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("gambar"));
        verify(portfolioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tambah karya gagal jika format berkas tidak didukung (contoh: GIF atau PDF)")
    void createPortfolio_unsupportedFormat_throwsException() {
        CreatePortfolioDto dto = new CreatePortfolioDto(
                "Animasi Logo",
                "Deskripsi",
                "https://example.com/animasi.gif"
        );

        AppException ex = assertThrows(AppException.class, () -> portfolioService.createPortfolio(10L, dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("Format berkas tidak didukung"));
    }

    @Test
    @DisplayName("Tambah karya gagal jika Base64 data format bukan JPG/PNG/WebP")
    void createPortfolio_unsupportedBase64_throwsException() {
        CreatePortfolioDto dto = new CreatePortfolioDto(
                "Dokumen Karya",
                "Deskripsi",
                "data:application/pdf;base64,JVBERi0xLjQK..."
        );

        AppException ex = assertThrows(AppException.class, () -> portfolioService.createPortfolio(10L, dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("Format berkas tidak didukung"));
    }

    @Test
    @DisplayName("Tambah karya gagal jika ukuran berkas melebihi 5 MB")
    void createPortfolio_fileSizeExceeds5MB_throwsException() {
        // 5 MB = 5 * 1024 * 1024 = 5242880 bytes. Base64 length ~ 7,000,000 chars
        String largeBase64Data = "A".repeat(7_100_000);
        String base64Image = "data:image/jpeg;base64," + largeBase64Data;

        CreatePortfolioDto dto = new CreatePortfolioDto(
                "Foto Ukuran Raksasa",
                "Deskripsi",
                base64Image
        );

        AppException ex = assertThrows(AppException.class, () -> portfolioService.createPortfolio(10L, dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("5 MB"));
    }

    @Test
    @DisplayName("Ambil portofolio saya berhasil mengembalikan daftar karya aktif")
    void getMyPortfolios_success() {
        when(freelancerProfileRepository.findByUserId(10L)).thenReturn(Optional.of(freelancerProfile));
        when(portfolioRepository.findByFreelancerProfileIdAndActiveTrueOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(samplePortfolio));

        List<PortfolioResponse> list = portfolioService.getMyPortfolios(10L);

        assertEquals(1, list.size());
        assertEquals("Desain Identitas Brand & Logo", list.get(0).getTitle());
    }

    @Test
    @DisplayName("Ambil portofolio publik berdasarkan freelancerProfileId")
    void getPortfoliosByFreelancerProfileId_success() {
        when(portfolioRepository.findByFreelancerProfileIdAndActiveTrueOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(samplePortfolio));

        List<PortfolioResponse> list = portfolioService.getPortfoliosByFreelancerProfileId(1L);

        assertEquals(1, list.size());
        assertEquals("Budi Studio", list.get(0).getFreelancerName());
    }

    @Test
    @DisplayName("Ambil detail satu karya portofolio berhasil")
    void getPortfolioById_success() {
        when(portfolioRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(samplePortfolio));

        PortfolioResponse response = portfolioService.getPortfolioById(100L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
    }

    @Test
    @DisplayName("Ambil detail portofolio gagal jika ID tidak ditemukan")
    void getPortfolioById_notFound_throwsException() {
        when(portfolioRepository.findByIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> portfolioService.getPortfolioById(999L));
    }

    @Test
    @DisplayName("Ubah karya portofolio berhasil jika pengguna adalah pemilik")
    void updatePortfolio_success() {
        UpdatePortfolioDto dto = new UpdatePortfolioDto(
                "Desain Identitas Brand & Logo V2",
                "Revisi palet warna",
                "https://images.unsplash.com/photo-update.webp"
        );

        when(portfolioRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(samplePortfolio));
        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(inv -> inv.getArgument(0));

        PortfolioResponse updated = portfolioService.updatePortfolio(10L, 100L, dto);

        assertNotNull(updated);
        assertEquals("Desain Identitas Brand & Logo V2", updated.getTitle());
        assertEquals("Revisi palet warna", updated.getDescription());
    }

    @Test
    @DisplayName("Ubah karya gagal dengan 403 Forbidden jika pengguna bukan pemilik")
    void updatePortfolio_forbidden_whenNotOwner() {
        UpdatePortfolioDto dto = new UpdatePortfolioDto("Ubah Judul", "Deskripsi", "https://img.jpg");

        when(portfolioRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(samplePortfolio));

        AppException ex = assertThrows(AppException.class, () -> portfolioService.updatePortfolio(99L, 100L, dto));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertTrue(ex.getMessage().contains("tidak memiliki akses"));
        verify(portfolioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Hapus karya portofolio berhasil dengan soft delete (is_active = false)")
    void deletePortfolio_success() {
        when(portfolioRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(samplePortfolio));
        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(inv -> inv.getArgument(0));

        portfolioService.deletePortfolio(10L, 100L);

        assertFalse(samplePortfolio.isActive());
        verify(portfolioRepository).save(samplePortfolio);
    }

    @Test
    @DisplayName("Hapus karya gagal dengan 403 Forbidden jika pengguna bukan pemilik")
    void deletePortfolio_forbidden_whenNotOwner() {
        when(portfolioRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(samplePortfolio));

        AppException ex = assertThrows(AppException.class, () -> portfolioService.deletePortfolio(99L, 100L));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertTrue(samplePortfolio.isActive());
    }
}
