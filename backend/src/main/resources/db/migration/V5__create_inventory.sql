CREATE TABLE inventory (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    medicine_id        BIGINT       NOT NULL,
    available_quantity INT          NOT NULL DEFAULT 0,
    reserved_quantity  INT          NOT NULL DEFAULT 0,
    reorder_level      INT          NOT NULL DEFAULT 10,
    last_updated       DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_inventory_medicine (medicine_id),
    CONSTRAINT fk_inventory_medicine FOREIGN KEY (medicine_id) REFERENCES medicines (id) ON DELETE CASCADE,
    CONSTRAINT chk_inventory_available_nonneg CHECK (available_quantity >= 0),
    CONSTRAINT chk_inventory_reserved_nonneg CHECK (reserved_quantity >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;