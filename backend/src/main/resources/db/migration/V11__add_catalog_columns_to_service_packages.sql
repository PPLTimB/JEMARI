-- PBI 03: Penyelarasan kolom katalog ke tabel service_packages

ALTER TABLE service_packages
    ADD COLUMN freelancer_profile_id BIGINT NULL AFTER freelancer_id,
    ADD COLUMN category_id BIGINT NULL AFTER category;

ALTER TABLE service_packages
    ADD CONSTRAINT fk_sp_freelancer_profile
        FOREIGN KEY (freelancer_profile_id) REFERENCES freelancer_profiles(id)
        ON DELETE SET NULL,
    ADD CONSTRAINT fk_sp_category
        FOREIGN KEY (category_id) REFERENCES service_categories(id)
        ON DELETE SET NULL;

CREATE INDEX idx_service_packages_freelancer_profile ON service_packages(freelancer_profile_id);
CREATE INDEX idx_service_packages_category_id ON service_packages(category_id);
