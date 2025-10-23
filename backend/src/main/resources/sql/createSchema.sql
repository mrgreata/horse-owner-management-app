CREATE TABLE IF NOT EXISTS owner
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255)
    );

CREATE TABLE IF NOT EXISTS horse
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(4095),
    date_of_birth DATE NOT NULL,
    sex ENUM('MALE', 'FEMALE') NOT NULL,
    owner_id BIGINT,
    image_path VARCHAR(1024),
    image_content_type VARCHAR(255),
    CONSTRAINT fk_horse_owner
    FOREIGN KEY (owner_id) REFERENCES owner(id)
    ON DELETE SET NULL
    );

-- Pferde Eltern

ALTER TABLE horse ADD COLUMN IF NOT EXISTS mother_id BIGINT;
ALTER TABLE horse ADD COLUMN IF NOT EXISTS father_id BIGINT;

ALTER TABLE horse
    ADD CONSTRAINT IF NOT EXISTS fk_horse_mother
    FOREIGN KEY (mother_id) REFERENCES horse(id) ON DELETE SET NULL;

ALTER TABLE horse
    ADD CONSTRAINT IF NOT EXISTS fk_horse_father
    FOREIGN KEY (father_id) REFERENCES horse(id) ON DELETE SET NULL;


ALTER TABLE horse
    ADD CONSTRAINT IF NOT EXISTS ck_horse_parent_distinct
    CHECK (mother_id IS NULL OR father_id IS NULL OR mother_id <> father_id);

ALTER TABLE horse
    ADD CONSTRAINT IF NOT EXISTS ck_horse_not_self_parent
    CHECK (
    (mother_id IS NULL OR mother_id <> id)
    AND (father_id IS NULL OR father_id <> id)
    );

