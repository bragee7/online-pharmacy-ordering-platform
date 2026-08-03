CREATE TABLE payments (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    order_id          BIGINT        NOT NULL,
    amount            DECIMAL(10,2) NOT NULL,
    method            VARCHAR(20)   NOT NULL,
    status            VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    transaction_id    VARCHAR(64)   NULL,
    gateway_reference VARCHAR(128)  NULL,
    paid_at           DATETIME(6)   NULL,
    created_at        DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payments_order (order_id),
    KEY idx_payments_status (status),
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;