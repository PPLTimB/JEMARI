package com.jemari.request;

import com.jemari.common.exception.AppException;
import com.jemari.common.exception.ResourceNotFoundException;
import com.jemari.request.dto.CreateRequestDto;
import com.jemari.request.dto.RequestResponse;
import com.jemari.request.dto.UpdateRequestDto;
import com.jemari.umkm.UmkmProfile;
import com.jemari.umkm.UmkmProfileRepository;
import com.jemari.user.Role;
import com.jemari.user.User;
import com.jemari.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private UmkmProfileRepository umkmProfileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DefaultRequestService requestService;

    private User umkmUser;
    private UmkmProfile umkmProfile;
    private ServiceRequest testRequest;

    @BeforeEach
    void setUp() {
        umkmUser = new User();
        umkmUser.setEmail("umkm@jemari.com");
        umkmUser.setRole(Role.UMKM);
        umkmUser.setActive(true);

        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(umkmUser, 1L);
        } catch (Exception ignored) {}

        umkmProfile = new UmkmProfile();
        umkmProfile.setUser(umkmUser);
        umkmProfile.setBusinessName("Warung Barokah");
        umkmProfile.setCategory("Kuliner");
        umkmProfile.setActive(true);

        try {
            var profileIdField = UmkmProfile.class.getDeclaredField("id");
            profileIdField.setAccessible(true);
            profileIdField.set(umkmProfile, 10L);
        } catch (Exception ignored) {}

        testRequest = new ServiceRequest(
                umkmProfile,
                "Butuh Foto Produk Makanan",
                "Fotografi",
                "Foto 10 menu makanan untuk GoFood",
                new BigDecimal("250000"),
                LocalDate.now().plusDays(7)
        );
        testRequest.setId(100L);
    }

    @Test
    void createRequest_Success() {
        CreateRequestDto dto = new CreateRequestDto(
                "Butuh Foto Produk Makanan",
                "Fotografi",
                "Foto 10 menu makanan untuk GoFood",
                new BigDecimal("250000"),
                LocalDate.now().plusDays(7)
        );

        when(userRepository.findByEmail("umkm@jemari.com")).thenReturn(Optional.of(umkmUser));
        when(umkmProfileRepository.findByUserId(1L)).thenReturn(Optional.of(umkmProfile));
        when(requestRepository.save(any(ServiceRequest.class))).thenAnswer(invocation -> {
            ServiceRequest r = invocation.getArgument(0);
            r.setId(101L);
            return r;
        });

        RequestResponse res = requestService.createRequest(dto, "umkm@jemari.com");

        assertNotNull(res);
        assertEquals(101L, res.getId());
        assertEquals("Butuh Foto Produk Makanan", res.getTitle());
        assertEquals(RequestStatus.OPEN, res.getStatus());
        assertEquals("Dibuka", res.getStatusLabel());
        verify(requestRepository, times(1)).save(any(ServiceRequest.class));
    }

    @Test
    void createRequest_Fail_RoleNotUmkm() {
        User freelancerUser = new User();
        freelancerUser.setEmail("freelancer@jemari.com");
        freelancerUser.setRole(Role.FREELANCER);

        CreateRequestDto dto = new CreateRequestDto(
                "Judul", "Kategori", "Deskripsi", new BigDecimal("100000"), LocalDate.now().plusDays(5)
        );

        when(userRepository.findByEmail("freelancer@jemari.com")).thenReturn(Optional.of(freelancerUser));

        AppException ex = assertThrows(AppException.class, () ->
                requestService.createRequest(dto, "freelancer@jemari.com"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void createRequest_Fail_BudgetZeroOrNegative() {
        CreateRequestDto dto = new CreateRequestDto(
                "Judul", "Kategori", "Deskripsi", BigDecimal.ZERO, LocalDate.now().plusDays(5)
        );

        when(userRepository.findByEmail("umkm@jemari.com")).thenReturn(Optional.of(umkmUser));

        AppException ex = assertThrows(AppException.class, () ->
                requestService.createRequest(dto, "umkm@jemari.com"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("Anggaran"));
    }

    @Test
    void createRequest_Fail_DeadlinePast() {
        CreateRequestDto dto = new CreateRequestDto(
                "Judul", "Kategori", "Deskripsi", new BigDecimal("100000"), LocalDate.now().minusDays(1)
        );

        when(userRepository.findByEmail("umkm@jemari.com")).thenReturn(Optional.of(umkmUser));

        AppException ex = assertThrows(AppException.class, () ->
                requestService.createRequest(dto, "umkm@jemari.com"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("lampau"));
    }

    @Test
    void getMyRequests_Success() {
        when(userRepository.findByEmail("umkm@jemari.com")).thenReturn(Optional.of(umkmUser));
        when(umkmProfileRepository.findByUserId(1L)).thenReturn(Optional.of(umkmProfile));
        when(requestRepository.findByUmkmProfileIdAndActiveTrueOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(testRequest));

        List<RequestResponse> list = requestService.getMyRequests("umkm@jemari.com");

        assertEquals(1, list.size());
        assertEquals("Butuh Foto Produk Makanan", list.get(0).getTitle());
    }

    @Test
    void getAllOpenRequests_WithoutCategory() {
        when(requestRepository.findByStatusAndActiveTrueOrderByCreatedAtDesc(RequestStatus.OPEN))
                .thenReturn(List.of(testRequest));

        List<RequestResponse> list = requestService.getAllOpenRequests(null);

        assertEquals(1, list.size());
    }

    @Test
    void getAllOpenRequests_WithCategory() {
        when(requestRepository.findByCategoryIgnoreCaseAndStatusAndActiveTrueOrderByCreatedAtDesc("Fotografi", RequestStatus.OPEN))
                .thenReturn(List.of(testRequest));

        List<RequestResponse> list = requestService.getAllOpenRequests("Fotografi");

        assertEquals(1, list.size());
    }

    @Test
    void getRequestById_Success() {
        when(requestRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(testRequest));

        RequestResponse res = requestService.getRequestById(100L);

        assertNotNull(res);
        assertEquals(100L, res.getId());
    }

    @Test
    void getRequestById_NotFound() {
        when(requestRepository.findByIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> requestService.getRequestById(999L));
    }

    @Test
    void updateRequest_Success() {
        UpdateRequestDto dto = new UpdateRequestDto(
                "Judul Baru", "Kategori Baru", "Deskripsi Baru", new BigDecimal("300000"), LocalDate.now().plusDays(10)
        );

        when(requestRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(testRequest));
        when(userRepository.findByEmail("umkm@jemari.com")).thenReturn(Optional.of(umkmUser));
        when(requestRepository.save(any(ServiceRequest.class))).thenReturn(testRequest);

        RequestResponse updated = requestService.updateRequest(100L, dto, "umkm@jemari.com");

        assertNotNull(updated);
        assertEquals("Judul Baru", testRequest.getTitle());
        assertEquals(new BigDecimal("300000"), testRequest.getBudget());
    }

    @Test
    void updateRequest_Fail_NotOwner() {
        User otherUser = new User();
        otherUser.setEmail("other@jemari.com");
        otherUser.setRole(Role.UMKM);
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(otherUser, 999L);
        } catch (Exception ignored) {}

        UpdateRequestDto dto = new UpdateRequestDto(
                "Judul Baru", "Kategori", "Deskripsi", new BigDecimal("300000"), LocalDate.now().plusDays(10)
        );

        when(requestRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(testRequest));
        when(userRepository.findByEmail("other@jemari.com")).thenReturn(Optional.of(otherUser));

        AppException ex = assertThrows(AppException.class, () ->
                requestService.updateRequest(100L, dto, "other@jemari.com"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void updateRequest_Fail_StatusClosed() {
        testRequest.setStatus(RequestStatus.CLOSED);

        UpdateRequestDto dto = new UpdateRequestDto(
                "Judul Baru", "Kategori", "Deskripsi", new BigDecimal("300000"), LocalDate.now().plusDays(10)
        );

        when(requestRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(testRequest));
        when(userRepository.findByEmail("umkm@jemari.com")).thenReturn(Optional.of(umkmUser));

        AppException ex = assertThrows(AppException.class, () ->
                requestService.updateRequest(100L, dto, "umkm@jemari.com"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void closeRequest_Success() {
        when(requestRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(testRequest));
        when(userRepository.findByEmail("umkm@jemari.com")).thenReturn(Optional.of(umkmUser));
        when(requestRepository.save(any(ServiceRequest.class))).thenReturn(testRequest);

        RequestResponse closed = requestService.closeRequest(100L, "umkm@jemari.com");

        assertNotNull(closed);
        assertEquals(RequestStatus.CLOSED, testRequest.getStatus());
    }

    @Test
    void closeRequest_Fail_AlreadyClosed() {
        testRequest.setStatus(RequestStatus.CLOSED);

        when(requestRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(testRequest));
        when(userRepository.findByEmail("umkm@jemari.com")).thenReturn(Optional.of(umkmUser));

        AppException ex = assertThrows(AppException.class, () ->
                requestService.closeRequest(100L, "umkm@jemari.com"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void deleteRequest_Success() {
        when(requestRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(testRequest));
        when(userRepository.findByEmail("umkm@jemari.com")).thenReturn(Optional.of(umkmUser));

        requestService.deleteRequest(100L, "umkm@jemari.com");

        assertFalse(testRequest.isActive());
        verify(requestRepository, times(1)).save(testRequest);
    }
}
