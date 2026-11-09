package com.jemari.catalog.category;

import com.jemari.catalog.category.dto.CreateKategoriRequest;
import com.jemari.catalog.category.dto.KategoriResponse;
import com.jemari.catalog.category.dto.UpdateKategoriRequest;
import com.jemari.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/kategori")
@PreAuthorize("hasRole('ADMIN')")
public class AdminKategoriController {

    private final KategoriService kategoriService;

    public AdminKategoriController(KategoriService kategoriService) {
        this.kategoriService = kategoriService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<KategoriResponse>> createKategori(
            @Valid @RequestBody CreateKategoriRequest request
    ) {
        KategoriResponse response = kategoriService.createKategori(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Kategori berhasil dibuat", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<KategoriResponse>>> getAllKategori() {
        List<KategoriResponse> response = kategoriService.getAllKategori();
        return ResponseEntity.ok(ApiResponse.ok("Daftar kategori berhasil diambil", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KategoriResponse>> getKategoriById(@PathVariable Long id) {
        KategoriResponse response = kategoriService.getKategoriById(id);
        return ResponseEntity.ok(ApiResponse.ok("Detail kategori berhasil diambil", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<KategoriResponse>> updateKategori(
            @PathVariable Long id,
            @Valid @RequestBody UpdateKategoriRequest request
    ) {
        KategoriResponse response = kategoriService.updateKategori(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Kategori berhasil diperbarui", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteKategori(@PathVariable Long id) {
        kategoriService.deleteKategori(id);
        return ResponseEntity.ok(ApiResponse.ok("Kategori berhasil dihapus", null));
    }
}

