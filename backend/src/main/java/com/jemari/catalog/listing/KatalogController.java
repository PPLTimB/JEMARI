package com.jemari.catalog.listing;

import com.jemari.catalog.listing.dto.KatalogFilterParam;
import com.jemari.catalog.listing.dto.KatalogPagedResponse;
import com.jemari.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/katalog")
@PreAuthorize("hasRole('UMKM')")
public class KatalogController {

    private final KatalogService katalogService;

    public KatalogController(KatalogService katalogService) {
        this.katalogService = katalogService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<KatalogPagedResponse>> searchKatalog(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long kategoriId,
            @RequestParam(required = false) BigDecimal hargaMin,
            @RequestParam(required = false) BigDecimal hargaMax,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        KatalogFilterParam param = new KatalogFilterParam(keyword, kategoriId, hargaMin, hargaMax, sort, page, size);
        ApiResponse<KatalogPagedResponse> response = katalogService.searchKatalog(param);
        return ResponseEntity.ok(response);
    }
}

