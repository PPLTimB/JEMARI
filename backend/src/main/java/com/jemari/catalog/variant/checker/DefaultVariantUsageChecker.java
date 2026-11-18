package com.jemari.catalog.variant.checker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementasi default VariantUsageChecker.
 * Saat ini tabel/modul pesanan (order) belum ada di sistem, sehingga implementasi
 * ini mengembalikan false ("varian belum dipakai pesanan berjalan").
 * Implementasi sebenarnya akan disesuaikan saat modul order selesai dikembangkan.
 */
@Component
public class DefaultVariantUsageChecker implements VariantUsageChecker {

    private static final Logger log = LoggerFactory.getLogger(DefaultVariantUsageChecker.class);

    @Override
    public boolean isVariantInActiveOrder(Long variantId) {
        log.debug("Pengecekan keterikatan pesanan untuk varian ID: {}. Menunggu implementasi modul order.", variantId);
        // Belum ada tabel pesanan, default mengembalikan false
        return false;
    }
}
