-- V1__init.sql — MediCare Pharmacy initial schema (PostgreSQL)
-- Matches JPA entities exactly (table/column names, types, constraints).

-- ================= users =================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    email VARCHAR NOT NULL UNIQUE,
    password VARCHAR NOT NULL,
    phone VARCHAR,
    address TEXT,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
-- idx_users_email covered by UNIQUE constraint on users(email)
-- (matches @Table(indexes = @Index(name = "idx_users_email", columnList = "email", unique = true)))

-- ================= categories =================
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP
);

-- ================= medicines =================
CREATE TABLE medicines (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    generic_name VARCHAR,
    brand_name VARCHAR,
    description TEXT,
    manufacturer VARCHAR,
    price NUMERIC(10, 2) NOT NULL,
    prescription_required BOOLEAN NOT NULL DEFAULT FALSE,
    category_id BIGINT REFERENCES categories (id),
    image_url VARCHAR,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- ================= inventory =================
CREATE TABLE inventory (
    id BIGSERIAL PRIMARY KEY,
    medicine_id BIGINT NOT NULL UNIQUE REFERENCES medicines (id) ON DELETE CASCADE,
    quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    reorder_level INT NOT NULL DEFAULT 10,
    updated_at TIMESTAMP
);

-- ================= prescriptions =================
CREATE TABLE prescriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id),
    file_name VARCHAR NOT NULL,
    file_path VARCHAR NOT NULL,
    file_type VARCHAR,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    uploaded_at TIMESTAMP,
    reviewed_at TIMESTAMP,
    reviewed_by BIGINT REFERENCES users (id),
    rejection_reason TEXT
);

-- ================= carts =================
CREATE TABLE carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- ================= cart_items =================
CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL REFERENCES carts (id) ON DELETE CASCADE,
    medicine_id BIGINT NOT NULL REFERENCES medicines (id),
    quantity INT NOT NULL DEFAULT 1,
    CONSTRAINT uq_cart_medicine UNIQUE (cart_id, medicine_id)
);

-- ================= orders (plural table name) =================
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id),
    order_number VARCHAR NOT NULL UNIQUE,
    total_amount NUMERIC(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PLACED',
    shipping_address TEXT NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- ================= order_items =================
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    medicine_id BIGINT NOT NULL REFERENCES medicines (id),
    quantity INT NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL,
    subtotal NUMERIC(12, 2) NOT NULL
);

-- ================= payments =================
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE REFERENCES orders (id),
    amount NUMERIC(12, 2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL DEFAULT 'COD',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    transaction_reference VARCHAR UNIQUE,
    paid_at TIMESTAMP,
    created_at TIMESTAMP
);

-- ================= deliveries =================
CREATE TABLE deliveries (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE REFERENCES orders (id),
    tracking_number VARCHAR NOT NULL UNIQUE,
    carrier VARCHAR DEFAULT 'MediCare Logistics',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    estimated_delivery_date DATE,
    delivered_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- ================= audit_logs =================
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    timestamp TIMESTAMP NOT NULL DEFAULT now(),
    details TEXT
);

-- ================= indexes =================
CREATE INDEX IF NOT EXISTS idx_medicines_name ON medicines (name);
CREATE INDEX IF NOT EXISTS idx_medicines_category ON medicines (category_id);
CREATE INDEX IF NOT EXISTS idx_presc_user ON prescriptions (user_id);
CREATE INDEX IF NOT EXISTS idx_presc_status ON prescriptions (status);
CREATE INDEX IF NOT EXISTS idx_orders_user ON orders (user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders (status);
