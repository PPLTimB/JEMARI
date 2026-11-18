package com.jemari.catalog.variant.checker;

/**
 * Interface pengecekan apakah suatu varian paket jasa sedang digunakan
 * oleh pesanan (order) yang sedang berjalan.
 *
 * Sesuai aturan bisnis UC-18:
 * "Perubahan harga atau durasi hanya berlaku untuk pemesanan baru,
 * sedangkan pesanan yang sudah berjalan tetap memakai ketentuan awal."
 */
public interface VariantUsageChecker {

    /**
     * Memeriksa apakah varian paket sedang aktif digunakan dalam pesanan berjalan.
     *
     * @param variantId ID varian paket yang diperiksa
     * @return true jika varian sedang dipakai dalam pesanan aktif, false jika tidak
     */
    boolean isVariantInActiveOrder(Long variantId);
}
