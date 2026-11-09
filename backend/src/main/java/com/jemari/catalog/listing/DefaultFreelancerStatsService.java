package com.jemari.catalog.listing;

import org.springframework.stereotype.Service;

/**
 * Implementasi default sementara untuk FreelancerStatsService.
 * Mengembalikan 0 karena tabel transaksi/order belum ada di tahap ini (Sprint 1 / PBI-03).
 */
@Service
public class DefaultFreelancerStatsService implements FreelancerStatsService {

    @Override
    public int getCompletedOrderCount(Long freelancerProfileId) {
        // Mengembalikan 0 sampai modul order/transaksi diimplementasikan.
        return 0;
    }
}

