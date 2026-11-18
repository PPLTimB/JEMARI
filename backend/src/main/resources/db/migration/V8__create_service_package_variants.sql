-- PBI-04 (JRI-17): Menampilkan Detail Lengkap Paket Jasa (UC-03, UC-18, FR-02)
-- Tabel untuk menyimpan varian harga dan estimasi durasi pada paket jasa

CREATE TABLE IF NOT EXISTS service_package_variants (
    id             BIGINT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    package_id     BIGINT         NOT NULL,
    name           VARCHAR(100)   NOT NULL,
    description    VARCHAR(255)   NULL,
    price          DECIMAL(12, 2) NOT NULL,
    estimated_days INT            NOT NULL,
    is_active      TINYINT(1)     NOT NULL DEFAULT 1,
    created_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_service_package_variants_price CHECK (price > 0),
    CONSTRAINT chk_service_package_variants_estimated_days CHECK (estimated_days >= 1)
    -- CATATAN FOREIGN KEY:
    -- Tabel service_packages (PBI-02, V3) belum ada di basis branch ini.
    -- Foreign key ke service_packages(id) akan ditambahkan melalui migrasi terpisah
    -- setelah migrasi V3 masuk ke basis branch, misalnya dengan perintah:
    -- ALTER TABLE service_package_variants
    --     ADD CONSTRAINT fk_service_package_variants_package
    --     FOREIGN KEY (package_id) REFERENCES service_packages(id)
    --     ON DELETE CASCADE;
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indeks pendukung query pencarian varian per paket dan status aktif
CREATE INDEX idx_service_package_variants_package_id ON service_package_variants(package_id);
CREATE INDEX idx_service_package_variants_is_active ON service_package_variants(is_active);
CREATE INDEX idx_service_package_variants_pkg_active ON service_package_variants(package_id, is_active);
