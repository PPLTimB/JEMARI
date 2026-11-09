CREATE TABLE IF NOT EXISTS kategori_jasa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nama VARCHAR(100) NOT NULL,
    deskripsi TEXT NULL,
    parent_id BIGINT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_kategori_parent
        FOREIGN KEY (parent_id) REFERENCES kategori_jasa(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_kategori_jasa_parent_id ON kategori_jasa(parent_id);
CREATE INDEX idx_kategori_jasa_is_active ON kategori_jasa(is_active);
CREATE INDEX idx_kategori_jasa_nama ON kategori_jasa(nama);
