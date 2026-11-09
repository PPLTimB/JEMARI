-- Tabel minimal paket_jasa untuk keperluan katalog (PBI-03 / JRI-16)
-- Catatan koordinasi: Skema ini perlu disinkronkan dengan PBI-02 (dikerjakan Arsya).
CREATE TABLE IF NOT EXISTS paket_jasa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    freelancer_profile_id BIGINT NOT NULL,
    kategori_id BIGINT NOT NULL,
    judul VARCHAR(255) NOT NULL,
    deskripsi TEXT NOT NULL,
    harga_mulai DECIMAL(12,2) NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_paket_jasa_freelancer
        FOREIGN KEY (freelancer_profile_id) REFERENCES freelancer_profiles(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_paket_jasa_kategori
        FOREIGN KEY (kategori_id) REFERENCES kategori_jasa(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_paket_jasa_kategori_id ON paket_jasa(kategori_id);
CREATE INDEX idx_paket_jasa_harga_mulai ON paket_jasa(harga_mulai);
CREATE INDEX idx_paket_jasa_is_active ON paket_jasa(is_active);
CREATE INDEX idx_paket_jasa_freelancer_profile_id ON paket_jasa(freelancer_profile_id);
