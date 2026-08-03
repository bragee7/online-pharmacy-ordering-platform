CREATE TABLE prescriptions (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    reference        VARCHAR(40)   NOT NULL,
    user_id          BIGINT        NOT NULL,
    image_reference  VARCHAR(500)  NULL,
    status           VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    rejection_reason VARCHAR(500)  NULL,
    notes            VARCHAR(500)  NULL,
    doctor_name      VARCHAR(100)  NULL,
    expiry_date      DATE          NULL,
    reviewed_by      BIGINT        NULL,
    reviewed_at      DATETIME(6)   NULL,
    created_at       DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_prescriptions_reference (reference),
    KEY idx_prescriptions_user (user_id),
    KEY idx_prescriptions_status (status),
    CONSTRAINT fk_prescriptions_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_prescriptions_reviewer FOREIGN KEY (reviewed_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE prescription_items (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    prescription_id BIGINT        NOT NULL,
    medicine_id     BIGINT        NULL,
    medicine_name   VARCHAR(150)  NOT NULL,
    dosage          VARCHAR(100)  NULL,
    quantity        INT           NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    KEY idx_prescription_items_prescription (prescription_id),
    CONSTRAINT fk_prescription_items_prescription FOREIGN KEY (prescription_id) REFERENCES prescriptions (id) ON DELETE CASCADE,
    CONSTRAINT fk_prescription_items_medicine FOREIGN KEY (medicine_id) REFERENCES medicines (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;