ALTER TABLE umkm_profiles
    ADD COLUMN business_scale VARCHAR(20) NOT NULL AFTER category,
    ADD CONSTRAINT chk_umkm_profiles_business_scale
        CHECK (business_scale IN ('MIKRO', 'KECIL', 'MENENGAH', 'BESAR'));

CREATE INDEX idx_umkm_profiles_business_scale ON umkm_profiles(business_scale);
