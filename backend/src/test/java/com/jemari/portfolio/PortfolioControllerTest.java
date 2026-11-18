package com.jemari.portfolio;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jemari.common.exception.GlobalExceptionHandler;
import com.jemari.common.exception.ResourceNotFoundException;
import com.jemari.portfolio.dto.CreatePortfolioDto;
import com.jemari.portfolio.dto.PortfolioResponse;
import com.jemari.portfolio.dto.UpdatePortfolioDto;
import com.jemari.user.Role;
import com.jemari.user.User;
import com.jemari.user.UserPrincipal;

@ExtendWith(MockitoExtension.class)
class PortfolioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PortfolioService portfolioService;

    @InjectMocks
    private PortfolioController portfolioController;

    private ObjectMapper objectMapper;
    private UserPrincipal testPrincipal;
    private PortfolioResponse sampleResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        User user = new User();
        user.setId(10L);
        user.setEmail("freelancer@jemari.com");
        user.setRole(Role.FREELANCER);
        testPrincipal = new UserPrincipal(user);

        sampleResponse = new PortfolioResponse(
                100L,
                1L,
                "Budi Studio",
                "Desain Identitas Brand",
                "Konsep logo modern minimalis",
                "https://example.com/logo.jpg",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        HandlerMethodArgumentResolver principalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                        && parameter.getParameterType().equals(UserPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          WebDataBinderFactory binderFactory) {
                return testPrincipal;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(portfolioController)
                .setCustomArgumentResolvers(principalResolver)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/portfolios mengembalikan 201 Created dan data karya")
    void createPortfolio_returns201() throws Exception {
        CreatePortfolioDto dto = new CreatePortfolioDto(
                "Desain Banner Media Sosial",
                "10 banner feed IG",
                "https://example.com/banner.png"
        );

        when(portfolioService.createPortfolio(eq(10L), any(CreatePortfolioDto.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/api/portfolios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Karya portofolio berhasil ditambahkan"))
                .andExpect(jsonPath("$.data.id").value(100L))
                .andExpect(jsonPath("$.data.title").value("Desain Identitas Brand"));
    }

    @Test
    @DisplayName("POST /api/portfolios mengembalikan 400 Bad Request jika judul kosong")
    void createPortfolio_blankTitle_returns400() throws Exception {
        CreatePortfolioDto dto = new CreatePortfolioDto(
                "",
                "Deskripsi",
                "https://example.com/img.png"
        );

        mockMvc.perform(post("/api/portfolios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validasi input gagal"));
    }

    @Test
    @DisplayName("GET /api/portfolios/my mengembalikan 200 OK dan daftar karya")
    void getMyPortfolios_returns200() throws Exception {
        when(portfolioService.getMyPortfolios(10L)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/portfolios/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(100L))
                .andExpect(jsonPath("$.data[0].title").value("Desain Identitas Brand"));
    }

    @Test
    @DisplayName("GET /api/portfolios/freelancer/{id} mengembalikan 200 OK galeri publik")
    void getPortfoliosByFreelancer_returns200() throws Exception {
        when(portfolioService.getPortfoliosByFreelancerProfileId(1L)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/portfolios/freelancer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].freelancerName").value("Budi Studio"));
    }

    @Test
    @DisplayName("GET /api/portfolios/{id} mengembalikan 200 OK rincian karya")
    void getPortfolioById_returns200() throws Exception {
        when(portfolioService.getPortfolioById(100L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/portfolios/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(100L));
    }

    @Test
    @DisplayName("GET /api/portfolios/{id} mengembalikan 404 jika karya tidak ditemukan")
    void getPortfolioById_notFound_returns404() throws Exception {
        when(portfolioService.getPortfolioById(999L))
                .thenThrow(new ResourceNotFoundException("Karya portofolio tidak ditemukan"));

        mockMvc.perform(get("/api/portfolios/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Karya portofolio tidak ditemukan"));
    }

    @Test
    @DisplayName("PUT /api/portfolios/{id} mengembalikan 200 OK saat berhasil diperbarui")
    void updatePortfolio_returns200() throws Exception {
        UpdatePortfolioDto dto = new UpdatePortfolioDto(
                "Desain Identitas Brand Baru",
                "Deskripsi baru",
                "https://example.com/logo-baru.jpg"
        );

        when(portfolioService.updatePortfolio(eq(10L), eq(100L), any(UpdatePortfolioDto.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(put("/api/portfolios/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Karya portofolio berhasil diperbarui"));
    }

    @Test
    @DisplayName("DELETE /api/portfolios/{id} mengembalikan 200 OK saat berhasil dihapus")
    void deletePortfolio_returns200() throws Exception {
        doNothing().when(portfolioService).deletePortfolio(10L, 100L);

        mockMvc.perform(delete("/api/portfolios/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Karya portofolio berhasil dihapus"));
    }
}
