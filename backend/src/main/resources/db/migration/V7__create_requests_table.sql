-- PBI 05: Pembuatan Request Kebutuhan Jasa UMKM (UC-08, UC-10)

CREATE TABLE IF NOT EXISTS requests (
    id              BIGINT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    umkm_profile_id BIGINT         NOT NULL,
    title           VARCHAR(255)   NOT NULL,
    category        VARCHAR(100)   NOT NULL,
    description     TEXT           NOT NULL,
    budget          DECIMAL(15, 2) NOT NULL,
    deadline        DATE           NOT NULL,
    status          VARCHAR(20)    NOT NULL DEFAULT 'OPEN',
    is_active       TINYINT(1)     NOT NULL DEFAULT 1,
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_requests_umkm_profile
        FOREIGN KEY (umkm_profile_id) REFERENCES umkm_profiles(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_requests_status
        CHECK (status IN ('OPEN', 'IN_PROGRESS', 'COMPLETED', 'CLOSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_requests_umkm_profile_id ON requests(umkm_profile_id);
CREATE INDEX idx_requests_status ON requests(status);
CREATE INDEX idx_requests_category ON requests(category);
CREATE INDEX idx_requests_is_active ON requests(is_active);
