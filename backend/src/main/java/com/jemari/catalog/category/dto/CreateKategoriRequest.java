package com.jemari.catalog.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateKategoriRequest(
        @NotBlank(message = "Nama kategori wajib diisi")
        @Size(max = 100, message = "Nama kategori maksimal 100 karakter")
        String nama,

        String deskripsi,

        Long parentId
) {
}
