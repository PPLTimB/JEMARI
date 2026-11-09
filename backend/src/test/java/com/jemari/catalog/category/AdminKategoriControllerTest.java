package com.jemari.catalog.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jemari.catalog.category.dto.CreateKategoriRequest;
import com.jemari.catalog.category.dto.KategoriResponse;
import com.jemari.common.config.SecurityConfig;
import com.jemari.common.security.JwtAuthenticationFilter;
import com.jemari.common.security.JwtTokenProvider;
import com.jemari.user.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminKategoriController.class)
@Import(SecurityConfig.class)
class AdminKategoriControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KategoriService kategoriService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setupFilter() throws Exception {
        doAnswer(invocation -> {
            jakarta.servlet.ServletRequest request = invocation.getArgument(0);
            jakarta.servlet.ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("Admin diizinkan mengakses GET /api/admin/kategori")
    @WithMockUser(username = "admin@jemari.com", roles = {"ADMIN"})
    void getAllKategori_Admin_Returns200() throws Exception {
        KategoriResponse kat = new KategoriResponse(1L, "Desain", "Desc", null, null, true, LocalDateTime.now(), LocalDateTime.now());
        when(kategoriService.getAllKategori()).thenReturn(List.of(kat));

        mockMvc.perform(get("/api/admin/kategori"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].nama").value("Desain"));
    }

    @Test
    @DisplayName("Admin diizinkan membuat kategori POST /api/admin/kategori")
    @WithMockUser(username = "admin@jemari.com", roles = {"ADMIN"})
    void createKategori_Admin_Returns201() throws Exception {
        CreateKategoriRequest request = new CreateKategoriRequest("Teknologi", "Kategori Teknologi", null);
        KategoriResponse response = new KategoriResponse(10L, "Teknologi", "Kategori Teknologi", null, null, true, LocalDateTime.now(), LocalDateTime.now());

        when(kategoriService.createKategori(any())).thenReturn(response);

        mockMvc.perform(post("/api/admin/kategori")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10));
    }

    @Test
    @DisplayName("Non-Admin (UMKM) ditolak saat mengakses /api/admin/kategori (403 Forbidden)")
    @WithMockUser(username = "umkm@jemari.com", roles = {"UMKM"})
    void getAllKategori_NonAdmin_Returns403() throws Exception {
        mockMvc.perform(get("/api/admin/kategori"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Pengguna tanpa autentikasi ditolak saat mengakses /api/admin/kategori (403 Forbidden)")
    void getAllKategori_Anonymous_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/kategori"))
                .andExpect(status().isForbidden());
    }
}

