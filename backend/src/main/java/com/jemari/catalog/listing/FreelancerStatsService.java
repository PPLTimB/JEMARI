package com.jemari.catalog.listing;

/**
 * Interface untuk mengisolasi penyediaan statistik freelancer (seperti jumlah pesanan/proyek selesai).
 * Modul order/transaksi belum tersedia saat PBI-03 dikerjakan, sehingga komponen ini dapat
 * diimplementasikan ulang atau dihubungkan ke modul order ketika modul tersebut sudah siap.
 */
public interface FreelancerStatsService {

    /**
     * Mengembalikan jumlah proyek/pesanan yang telah diselesaikan oleh freelancer.
     *
     * @param freelancerProfileId ID profil freelancer
     * @return jumlah proyek yang berstatus selesai
     */
    int getCompletedOrderCount(Long freelancerProfileId);
}

