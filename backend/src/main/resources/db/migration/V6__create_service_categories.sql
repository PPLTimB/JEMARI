CREATE TABLE IF NOT EXISTS service_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    parent_id BIGINT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_service_categories_parent
        FOREIGN KEY (parent_id) REFERENCES service_categories(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_service_categories_parent_id ON service_categories(parent_id);
CREATE INDEX idx_service_categories_is_active ON service_categories(is_active);
CREATE INDEX idx_service_categories_name ON service_categories(name);

