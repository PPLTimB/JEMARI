package com.jemari.catalog.variant.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO response untuk rincian biaya paket jasa sisi UMKM (UC-03, FR-02).
 * Berisi harga mulai (harga varian termurah), ringkasan estimasi durasi, dan daftar varian aktif.
 */
public record RincianBiayaResponse(
    Long packageId,
    BigDecimal hargaMulai,
    EstimasiDurasiResponse ringkasanEstimasi,
    List<VariantItemResponse> varian
) {
}
