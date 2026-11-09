package com.jemari.catalog.listing.dto;

import java.math.BigDecimal;

public record KatalogFilterParam(
        String keyword,
        Long kategoriId,
        BigDecimal hargaMin,
        BigDecimal hargaMax,
        String sort,
        Integer page,
        Integer size
) {
}
