CREATE TABLE IF NOT EXISTS service_packages (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    freelancer_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    category VARCHAR(100) NOT NULL,
    description TEXT NULL,
    base_price DECIMAL(12, 2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_service_packages_freelancer
        FOREIGN KEY (freelancer_id) REFERENCES users(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_service_packages_category ON service_packages(category);
CREATE INDEX idx_service_packages_freelancer ON service_packages(freelancer_id);