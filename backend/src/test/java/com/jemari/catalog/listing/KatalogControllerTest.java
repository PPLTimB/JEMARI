package com.jemari.catalog.listing;

import com.jemari.catalog.listing.dto.KatalogPagedResponse;
import com.jemari.common.config.SecurityConfig;
import com.jemari.common.dto.ApiResponse;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KatalogController.class)
@Import(SecurityConfig.class)
class KatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KatalogService katalogService;

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
    @DisplayName("UMKM diizinkan mengakses GET /api/katalog")
    @WithMockUser(username = "umkm@jemari.com", roles = {"UMKM"})
    void searchKatalog_Umkm_Returns200() throws Exception {
        KatalogPagedResponse pagedResponse = new KatalogPagedResponse(Collections.emptyList(), 0, 10, 0, 0);
        when(katalogService.searchKatalog(any()))
                .thenReturn(ApiResponse.ok("Katalog berhasil dimuat", pagedResponse));

        mockMvc.perform(get("/api/katalog")
                        .param("keyword", "desain")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Non-UMKM (Freelancer) ditolak saat mengakses GET /api/katalog (403 Forbidden)")
    @WithMockUser(username = "freelancer@jemari.com", roles = {"FREELANCER"})
    void searchKatalog_Freelancer_Returns403() throws Exception {
        mockMvc.perform(get("/api/katalog"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Input harga bukan angka ditolak dengan 400 Bad Request dan menyebut kolom bermasalah")
    @WithMockUser(username = "umkm@jemari.com", roles = {"UMKM"})
    void searchKatalog_InvalidPriceFormat_Returns400WithFieldError() throws Exception {
        mockMvc.perform(get("/api/katalog")
                        .param("hargaMin", "bukan_angka"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].field").value("hargaMin"))
                .andExpect(jsonPath("$.errors[0].message").value(containsString("harus berupa nilai angka yang valid")));
    }
}

