-- PBI 11: Keahlian dan Pengalaman Kerja Freelancer (UC-17, UC-04)

-- 1. Keahlian freelancer
CREATE TABLE IF NOT EXISTS freelancer_skills (
    id                    BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    freelancer_profile_id BIGINT       NOT NULL,
    skill_name            VARCHAR(100) NOT NULL,
    skill_level           VARCHAR(20)  NULL,
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_freelancer_skills_profile
        FOREIGN KEY (freelancer_profile_id) REFERENCES freelancer_profiles(id)
        ON DELETE CASCADE,
    -- Satu freelancer tidak boleh punya keahlian bernama sama
    -- (collation utf8mb4_unicode_ci membuat huruf besar/kecil dianggap sama).
    CONSTRAINT uq_freelancer_skills_profile_name UNIQUE (freelancer_profile_id, skill_name),
    CONSTRAINT chk_freelancer_skills_name CHECK (CHAR_LENGTH(TRIM(skill_name)) > 0),
    CONSTRAINT chk_freelancer_skills_level
        CHECK (skill_level IS NULL OR skill_level IN ('PEMULA', 'MENENGAH', 'MAHIR'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Pengalaman kerja freelancer (end_date NULL = masih berjalan)
CREATE TABLE IF NOT EXISTS freelancer_experiences (
    id                    BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    freelancer_profile_id BIGINT        NOT NULL,
    position              VARCHAR(150)  NOT NULL,
    company_name          VARCHAR(150)  NOT NULL,
    start_date            DATE          NOT NULL,
    end_date              DATE          NULL,
    description           VARCHAR(1000) NULL,
    created_at            TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_freelancer_experiences_profile
        FOREIGN KEY (freelancer_profile_id) REFERENCES freelancer_profiles(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_freelancer_experiences_position CHECK (CHAR_LENGTH(TRIM(position)) > 0),
    CONSTRAINT chk_freelancer_experiences_company CHECK (CHAR_LENGTH(TRIM(company_name)) > 0),
    CONSTRAINT chk_freelancer_experiences_dates CHECK (end_date IS NULL OR end_date >= start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Index pendukung query profil publik (pengalaman terbaru lebih dulu)
CREATE INDEX idx_freelancer_experiences_profile_start
    ON freelancer_experiences(freelancer_profile_id, start_date DESC);
