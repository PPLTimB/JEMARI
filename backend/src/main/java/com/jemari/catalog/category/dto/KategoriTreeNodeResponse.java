package com.jemari.catalog.category.dto;

import java.util.List;

public record KategoriTreeNodeResponse(
        Long id,
        String nama,
        String deskripsi,
        List<KategoriTreeNodeResponse> subKategori
) {
}

