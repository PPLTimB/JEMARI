
-- Tambahkan kolom dengan nilai default untuk menjaga data lama
ALTER TABLE umkm_profiles
    ADD COLUMN business_scale VARCHAR(20) NOT NULL DEFAULT 'MIKRO'
    AFTER category;

-- Batasi pilihan skala usaha
ALTER TABLE umkm_profiles
    ADD CONSTRAINT chk_umkm_profiles_business_scale
    CHECK (business_scale IN ('MIKRO', 'KECIL', 'MENENGAH', 'BESAR'));

-- Index untuk pencarian berdasarkan skala usaha
CREATE INDEX idx_umkm_profiles_business_scale
    ON umkm_profiles(business_scale);
