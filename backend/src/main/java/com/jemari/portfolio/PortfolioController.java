package com.jemari.portfolio;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jemari.common.dto.ApiResponse;
import com.jemari.portfolio.dto.CreatePortfolioDto;
import com.jemari.portfolio.dto.PortfolioResponse;
import com.jemari.portfolio.dto.UpdatePortfolioDto;
import com.jemari.user.UserPrincipal;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<PortfolioResponse>> createPortfolio(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreatePortfolioDto dto) {
        PortfolioResponse response = portfolioService.createPortfolio(principal.getId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Karya portofolio berhasil ditambahkan", response));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<List<PortfolioResponse>>> getMyPortfolios(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<PortfolioResponse> response = portfolioService.getMyPortfolios(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Daftar portofolio saya berhasil diambil", response));
    }

    @GetMapping("/freelancer/{id}")
    public ResponseEntity<ApiResponse<List<PortfolioResponse>>> getPortfoliosByFreelancer(
            @PathVariable Long id) {
        List<PortfolioResponse> response = portfolioService.getPortfoliosByFreelancerProfileId(id);
        return ResponseEntity.ok(ApiResponse.ok("Galeri portofolio freelancer berhasil diambil", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PortfolioResponse>> getPortfolioById(
            @PathVariable Long id) {
        PortfolioResponse response = portfolioService.getPortfolioById(id);
        return ResponseEntity.ok(ApiResponse.ok("Detail karya portofolio berhasil diambil", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<PortfolioResponse>> updatePortfolio(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdatePortfolioDto dto) {
        PortfolioResponse response = portfolioService.updatePortfolio(principal.getId(), id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Karya portofolio berhasil diperbarui", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<Void>> deletePortfolio(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        portfolioService.deletePortfolio(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Karya portofolio berhasil dihapus", null));
    }
}
