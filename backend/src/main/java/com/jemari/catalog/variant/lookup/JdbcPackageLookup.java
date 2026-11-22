package com.jemari.catalog.variant.lookup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Implementasi PackageLookup berbasis JdbcTemplate.
 * Digunakan selama PBI-02 belum mengimplementasikan entitas JPA ServicePackage di branch ini.
 */
@Component
public class JdbcPackageLookup implements PackageLookup {

    private static final Logger log = LoggerFactory.getLogger(JdbcPackageLookup.class);
    private final JdbcTemplate jdbcTemplate;

    public JdbcPackageLookup(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean packageExists(Long packageId) {
        if (packageId == null) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM service_packages WHERE id = ?";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, packageId);
            return count != null && count > 0;
        } catch (DataAccessException e) {
            log.warn("Tabel service_packages mungkin belum dibuat di database: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isPackageActive(Long packageId) {
        if (packageId == null) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM service_packages WHERE id = ? AND is_active = 1";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, packageId);
            return count != null && count > 0;
        } catch (DataAccessException e) {
            log.warn("Gagal mengecek status aktif paket di database: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isPackageOwnedByFreelancer(Long packageId, String freelancerEmail) {
        if (packageId == null || freelancerEmail == null || freelancerEmail.isBlank()) {
            return false;
        }
        String sql = """
            SELECT COUNT(*)
            FROM service_packages sp
            JOIN freelancer_profiles fp ON sp.freelancer_profile_id = fp.id
            JOIN users u ON fp.user_id = u.id
            WHERE sp.id = ? AND u.email = ? AND sp.is_active = 1
            """;
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, packageId, freelancerEmail);
            return count != null && count > 0;
        } catch (DataAccessException e) {
            log.warn("Gagal memvalidasi kepemilikan paket oleh freelancer di database: {}", e.getMessage());
            return false;
        }
    }
}
