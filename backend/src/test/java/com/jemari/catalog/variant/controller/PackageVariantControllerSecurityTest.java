package com.jemari.catalog.variant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jemari.catalog.variant.dto.CreateVariantRequest;
import com.jemari.catalog.variant.dto.EstimasiDurasiResponse;
import com.jemari.catalog.variant.dto.RincianBiayaResponse;
import com.jemari.catalog.variant.dto.UpdateVariantRequest;
import com.jemari.catalog.variant.dto.VariantItemResponse;
import com.jemari.catalog.variant.dto.VariantResponse;
import com.jemari.catalog.variant.service.ServicePackageVariantService;
import com.jemari.common.config.SecurityConfig;
import com.jemari.common.exception.GlobalExceptionHandler;
import com.jemari.common.security.JwtAuthenticationFilter;
import com.jemari.common.security.JwtTokenProvider;
import com.jemari.user.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {FreelancerPackageVariantController.class, UmkmPackageVariantController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class PackageVariantControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ServicePackageVariantService variantService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private final Long packageId = 1L;
    private final Long variantId = 10L;

    @Nested
    @DisplayName("Pengujian Autorisasi Role & Akses Endpoint")
    class AuthorizationTests {

        @Test
        @DisplayName("Role FREELANCER dapat mengakses GET /api/freelancer/paket/{paketId}/varian")
        @WithMockUser(username = "freelancer@jemari.com", roles = {"FREELANCER"})
        void getVariants_AsFreelancer_ReturnsOk() throws Exception {
            when(variantService.getVariantsForFreelancer(eq(packageId), eq("freelancer@jemari.com")))
                    .thenReturn(List.of(new VariantResponse(variantId, packageId, "Basic", "Deskripsi", new BigDecimal("50000.00"), 3, true, LocalDateTime.now(), LocalDateTime.now())));

            mockMvc.perform(get("/api/freelancer/paket/{paketId}/varian", packageId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].name", is("Basic")));
        }

        @Test
        @DisplayName("Role UMKM DITOLAK (403 Forbidden) saat mengakses /api/freelancer/**")
        @WithMockUser(username = "umkm@jemari.com", roles = {"UMKM"})
        void getVariants_AsUmkm_ReturnsForbidden() throws Exception {
            mockMvc.perform(get("/api/freelancer/paket/{paketId}/varian", packageId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Role ADMIN DITOLAK (403 Forbidden) saat mengakses /api/freelancer/**")
        @WithMockUser(username = "admin@jemari.com", roles = {"ADMIN"})
        void getVariants_AsAdmin_ReturnsForbidden() throws Exception {
            mockMvc.perform(get("/api/freelancer/paket/{paketId}/varian", packageId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Tanpa autentikasi DITOLAK saat mengakses /api/freelancer/**")
        void getVariants_Unauthenticated_ReturnsUnauthorizedOrForbidden() throws Exception {
            mockMvc.perform(get("/api/freelancer/paket/{paketId}/varian", packageId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Role UMKM dapat mengakses GET /api/paket/{paketId}/rincian-biaya")
        @WithMockUser(username = "umkm@jemari.com", roles = {"UMKM"})
        void getRincianBiaya_AsUmkm_ReturnsOk() throws Exception {
            RincianBiayaResponse response = new RincianBiayaResponse(
                    packageId,
                    new BigDecimal("50000.00"),
                    new EstimasiDurasiResponse(2, 5),
                    List.of(new VariantItemResponse(variantId, "Basic", "Desc", new BigDecimal("50000.00"), 2))
            );
            when(variantService.getRincianBiayaForUmkm(packageId)).thenReturn(response);

            mockMvc.perform(get("/api/paket/{paketId}/rincian-biaya", packageId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.hargaMulai", is(50000.00)))
                    .andExpect(jsonPath("$.data.ringkasanEstimasi.durasiTercepat", is(2)))
                    .andExpect(jsonPath("$.data.ringkasanEstimasi.durasiTerlama", is(5)))
                    .andExpect(jsonPath("$.data.varian", hasSize(1)));
        }

        @Test
        @DisplayName("Role FREELANCER DITOLAK (403 Forbidden) saat mengakses /api/paket/** (khusus UMKM)")
        @WithMockUser(username = "freelancer@jemari.com", roles = {"FREELANCER"})
        void getRincianBiaya_AsFreelancer_ReturnsForbidden() throws Exception {
            mockMvc.perform(get("/api/paket/{paketId}/rincian-biaya", packageId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Tanpa autentikasi DITOLAK saat mengakses /api/paket/{paketId}/rincian-biaya")
        void getRincianBiaya_Unauthenticated_ReturnsForbidden() throws Exception {
            mockMvc.perform(get("/api/paket/{paketId}/rincian-biaya", packageId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Pengujian Validasi Input DTO (UC-18)")
    class ValidationTests {

        @Test
        @DisplayName("POST varian dengan input valid mengembalikan 201 Created")
        @WithMockUser(username = "freelancer@jemari.com", roles = {"FREELANCER"})
        void createVariant_ValidInput_ReturnsCreated() throws Exception {
            CreateVariantRequest request = new CreateVariantRequest(
                    "Standard",
                    "Deskripsi varian",
                    new BigDecimal("75000.00"),
                    3
            );
            VariantResponse response = new VariantResponse(
                    variantId, packageId, "Standard", "Deskripsi varian",
                    new BigDecimal("75000.00"), 3, true, LocalDateTime.now(), LocalDateTime.now()
            );

            when(variantService.createVariant(eq(packageId), any(CreateVariantRequest.class), eq("freelancer@jemari.com")))
                    .thenReturn(response);

            mockMvc.perform(post("/api/freelancer/paket/{paketId}/varian", packageId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.name", is("Standard")))
                    .andExpect(jsonPath("$.data.price", is(75000.00)));
        }

        @Test
        @DisplayName("POST varian gagal validasi: nama kosong, harga nol/negatif, durasi di luar 1-365 hari")
        @WithMockUser(username = "freelancer@jemari.com", roles = {"FREELANCER"})
        void createVariant_InvalidFields_ReturnsBadRequestWithFieldErrors() throws Exception {
            // Kolom nama kosong (""), harga nol (0), estimasi durasi 0 (harus >= 1)
            CreateVariantRequest request = new CreateVariantRequest(
                    "",
                    "Deskripsi",
                    new BigDecimal("0.00"),
                    0
            );

            mockMvc.perform(post("/api/freelancer/paket/{paketId}/varian", packageId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", is("Validasi input gagal")))
                    .andExpect(jsonPath("$.errors[*].field", containsInAnyOrder("name", "price", "estimatedDays")));
        }

        @Test
        @DisplayName("POST varian gagal validasi: durasi melebihi 365 hari")
        @WithMockUser(username = "freelancer@jemari.com", roles = {"FREELANCER"})
        void createVariant_EstimatedDaysExceeds365_ReturnsBadRequest() throws Exception {
            CreateVariantRequest request = new CreateVariantRequest(
                    "Ultra Long",
                    "Deskripsi",
                    new BigDecimal("100000.00"),
                    400 // Melebihi batas maksimal 365
            );

            mockMvc.perform(post("/api/freelancer/paket/{paketId}/varian", packageId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.errors[0].field", is("estimatedDays")));
        }

        @Test
        @DisplayName("PUT varian dengan input valid mengembalikan 200 OK")
        @WithMockUser(username = "freelancer@jemari.com", roles = {"FREELANCER"})
        void updateVariant_ValidInput_ReturnsOk() throws Exception {
            UpdateVariantRequest request = new UpdateVariantRequest(
                    "Standard Revised",
                    "Deskripsi update",
                    new BigDecimal("80000.00"),
                    4
            );
            VariantResponse response = new VariantResponse(
                    variantId, packageId, "Standard Revised", "Deskripsi update",
                    new BigDecimal("80000.00"), 4, true, LocalDateTime.now(), LocalDateTime.now()
            );

            when(variantService.updateVariant(eq(packageId), eq(variantId), any(UpdateVariantRequest.class), eq("freelancer@jemari.com")))
                    .thenReturn(response);

            mockMvc.perform(put("/api/freelancer/paket/{paketId}/varian/{varianId}", packageId, variantId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.name", is("Standard Revised")));
        }

        @Test
        @DisplayName("DELETE varian berhasil mengembalikan 200 OK")
        @WithMockUser(username = "freelancer@jemari.com", roles = {"FREELANCER"})
        void deleteVariant_ReturnsOk() throws Exception {
            mockMvc.perform(delete("/api/freelancer/paket/{paketId}/varian/{varianId}", packageId, variantId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Berhasil menghapus varian paket")));
        }
    }
}
