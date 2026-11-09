package com.jemari.catalog.category;

import com.jemari.catalog.category.dto.KategoriTreeNodeResponse;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KategoriController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class KategoriControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    @DisplayName("UMKM diizinkan membaca pohon kategori aktif untuk dropdown")
    @WithMockUser(username = "umkm@jemari.com", roles = {"UMKM"})
    void getActiveKategoriTree_Umkm_Returns200() throws Exception {
        KategoriTreeNodeResponse node = new KategoriTreeNodeResponse(1L, "Desain", "Kategori desain", List.of());
        when(kategoriService.getActiveKategoriTree()).thenReturn(List.of(node));

        mockMvc.perform(get("/api/kategori"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].nama").value("Desain"));
    }

    @Test
    @DisplayName("Pengguna tanpa login ditolak saat mengakses GET /api/kategori")
    void getActiveKategoriTree_Anonymous_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/kategori"))
                .andExpect(status().isForbidden());
    }
}

