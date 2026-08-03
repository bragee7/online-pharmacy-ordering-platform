CREATE TABLE order_status_history (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    order_id    BIGINT       NOT NULL,
    from_status VARCHAR(25)  NULL,
    to_status   VARCHAR(25)  NOT NULL,
    changed_by  VARCHAR(120) NULL,
    note        VARCHAR(500) NULL,
    changed_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_osh_order (order_id),
    CONSTRAINT fk_osh_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;