package com.jemari.catalog.category;

import com.jemari.catalog.category.dto.KategoriTreeNodeResponse;
import com.jemari.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/kategori")
public class KategoriController {

    private final KategoriService kategoriService;

    public KategoriController(KategoriService kategoriService) {
        this.kategoriService = kategoriService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<KategoriTreeNodeResponse>>> getActiveKategoriTree() {
        List<KategoriTreeNodeResponse> tree = kategoriService.getActiveKategoriTree();
        return ResponseEntity.ok(ApiResponse.ok("Pohon kategori aktif berhasil diambil", tree));
    }
}

