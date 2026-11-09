package com.jemari.catalog.listing.dto;

import java.math.BigDecimal;

public record PaketJasaItemResponse(
        Long id,
        String judul,
        String namaKategori,
        FreelancerItemResponse freelancer,
        BigDecimal hargaMulai,
        boolean baru
) {
}
