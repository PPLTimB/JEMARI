package com.jemari.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jemari.common.exception.GlobalExceptionHandler;
import com.jemari.request.dto.CreateRequestDto;
import com.jemari.request.dto.RequestResponse;
import com.jemari.request.dto.UpdateRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RequestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RequestService requestService;

    @InjectMocks
    private RequestController requestController;

    private ObjectMapper objectMapper;
    private Principal mockPrincipal;
    private RequestResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(requestController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockPrincipal = () -> "umkm@jemari.com";

        sampleResponse = new RequestResponse();
        sampleResponse.setId(1L);
        sampleResponse.setTitle("Desain Logo Warung");
        sampleResponse.setCategory("Desain Grafis");
        sampleResponse.setDescription("Butuh logo minimalis warna coklat");
        sampleResponse.setBudget(new BigDecimal("150000"));
        sampleResponse.setDeadline(LocalDate.now().plusDays(5));
        sampleResponse.setStatus(RequestStatus.OPEN);
        sampleResponse.setStatusLabel("Dibuka");
    }

    @Test
    void createRequest_Success() throws Exception {
        CreateRequestDto dto = new CreateRequestDto(
                "Desain Logo Warung",
                "Desain Grafis",
                "Butuh logo minimalis warna coklat",
                new BigDecimal("150000"),
                LocalDate.now().plusDays(5)
        );

        when(requestService.createRequest(any(CreateRequestDto.class), eq("umkm@jemari.com")))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/api/requests")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Desain Logo Warung"))
                .andExpect(jsonPath("$.data.status").value("OPEN"));
    }

    @Test
    void createRequest_Fail_ValidationError() throws Exception {
        // Missing title, empty category, negative budget
        CreateRequestDto dto = new CreateRequestDto(
                "",
                "",
                "",
                new BigDecimal("-5000"),
                LocalDate.now().minusDays(2)
        );

        mockMvc.perform(post("/api/requests")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void getMyRequests_Success() throws Exception {
        when(requestService.getMyRequests("umkm@jemari.com"))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/requests/my")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void getAllOpenRequests_Success() throws Exception {
        when(requestService.getAllOpenRequests(any()))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Desain Logo Warung"));
    }

    @Test
    void getRequestById_Success() throws Exception {
        when(requestService.getRequestById(1L))
                .thenReturn(sampleResponse);

        mockMvc.perform(get("/api/requests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void updateRequest_Success() throws Exception {
        UpdateRequestDto dto = new UpdateRequestDto(
                "Judul Revisi",
                "Desain Grafis",
                "Deskripsi Revisi",
                new BigDecimal("200000"),
                LocalDate.now().plusDays(10)
        );

        when(requestService.updateRequest(eq(1L), any(UpdateRequestDto.class), eq("umkm@jemari.com")))
                .thenReturn(sampleResponse);

        mockMvc.perform(put("/api/requests/1")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void closeRequest_Success() throws Exception {
        sampleResponse.setStatus(RequestStatus.CLOSED);
        sampleResponse.setStatusLabel("Ditutup");

        when(requestService.closeRequest(1L, "umkm@jemari.com"))
                .thenReturn(sampleResponse);

        mockMvc.perform(patch("/api/requests/1/close")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CLOSED"));
    }

    @Test
    void deleteRequest_Success() throws Exception {
        mockMvc.perform(delete("/api/requests/1")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
