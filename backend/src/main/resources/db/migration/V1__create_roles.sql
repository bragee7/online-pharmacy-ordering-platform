-- Roles table (mirrors entity Role)
CREATE TABLE roles (
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    name VARCHAR(30)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_roles_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO roles (name) VALUES ('CUSTOMER'), ('PHARMACIST'), ('ADMIN');