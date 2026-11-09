package com.jemari.catalog.listing.dto;

import java.util.List;

public record KatalogPagedResponse(
        List<PaketJasaItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
