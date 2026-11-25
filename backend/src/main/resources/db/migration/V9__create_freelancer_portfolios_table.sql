-- =============================================================================
-- Migration V9: Create freelancer_portfolios table
-- PBI 06: Pengunggahan & Pengelolaan Item Karya Portofolio Freelancer (UC-17, FR-06, FR-03)
-- =============================================================================

CREATE TABLE IF NOT EXISTS freelancer_portfolios (
    id                    BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    freelancer_profile_id BIGINT        NOT NULL,
    title                 VARCHAR(150)  NOT NULL,
    description           TEXT          NULL,
    image_url             VARCHAR(255)  NOT NULL,
    is_active             TINYINT(1)    NOT NULL DEFAULT 1,
    created_at            TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_portfolios_freelancer_profile
        FOREIGN KEY (freelancer_profile_id) REFERENCES freelancer_profiles(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_portfolios_freelancer_profile_id ON freelancer_portfolios(freelancer_profile_id);
CREATE INDEX idx_portfolios_is_active ON freelancer_portfolios(is_active);
