-- PBI 09 (JRI-22): Profil Usaha UMKM (UC-01)

CREATE TABLE IF NOT EXISTS umkm_profiles (
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT       NOT NULL UNIQUE,
    business_name VARCHAR(150) NOT NULL,
    description   TEXT         NULL,
    category      VARCHAR(100) NOT NULL,
    phone_number  VARCHAR(20)  NULL,
    contact_email VARCHAR(255) NULL,
    is_active     TINYINT(1)   NOT NULL DEFAULT 1,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_umkm_profiles_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
 
CREATE TABLE IF NOT EXISTS umkm_product_photos (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    umkm_profile_id BIGINT       NOT NULL,
    photo_url       VARCHAR(255) NOT NULL,
    sort_order      INT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_umkm_product_photos_profile
        FOREIGN KEY (umkm_profile_id) REFERENCES umkm_profiles(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
 
CREATE INDEX idx_umkm_profiles_category ON umkm_profiles(category);
CREATE INDEX idx_umkm_profiles_is_active ON umkm_profiles(is_active);
CREATE INDEX idx_umkm_product_photos_profile ON umkm_product_photos(umkm_profile_id);