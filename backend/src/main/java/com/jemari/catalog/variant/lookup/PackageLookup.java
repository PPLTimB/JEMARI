package com.jemari.catalog.variant.lookup;

/**
 * Kontrak lookup informasi paket jasa ke tabel service_packages.
 * Diisolasi di interface ini agar implementasi dapat dengan mudah diganti
 * ketika PBI-02 (pembuatan & pengelolaan paket) selesai dan di-merge ke basis branch.
 */
public interface PackageLookup {

    /**
     * Memeriksa apakah paket dengan ID tertentu ada di sistem.
     *
     * @param packageId ID paket jasa
     * @return true jika paket ada, false jika tidak
     */
    boolean packageExists(Long packageId);

    /**
     * Memeriksa apakah paket dengan ID tertentu ada dan berstatus aktif (is_active = 1).
     *
     * @param packageId ID paket jasa
     * @return true jika paket aktif, false jika tidak ada atau dinonaktifkan
     */
    boolean isPackageActive(Long packageId);

    /**
     * Memeriksa apakah paket dengan ID tertentu dimiliki oleh freelancer dengan email yang diberikan.
     * Melakukan join: service_packages -> freelancer_profiles -> users.
     *
     * @param packageId       ID paket jasa
     * @param freelancerEmail Email user yang sedang login (dari Authentication.getName())
     * @return true jika paket aktif dan merupakan milik freelancer tersebut
     */
    boolean isPackageOwnedByFreelancer(Long packageId, String freelancerEmail);
}
