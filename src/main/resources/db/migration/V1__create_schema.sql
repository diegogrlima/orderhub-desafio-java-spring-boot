-- =====================================================================
-- OrderHub - esquema inicial
-- =====================================================================

CREATE TABLE app_users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password_hash   VARCHAR(100) NOT NULL,
    full_name       VARCHAR(120) NOT NULL,
    role            VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'SALES', 'FINANCE')),
    active          BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE categories (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(80)  NOT NULL UNIQUE,
    description     VARCHAR(255)
);

CREATE TABLE products (
    id              BIGSERIAL PRIMARY KEY,
    sku             VARCHAR(30)   NOT NULL UNIQUE,
    name            VARCHAR(150)  NOT NULL,
    category_id     BIGINT        NOT NULL REFERENCES categories (id),
    unit            VARCHAR(10)   NOT NULL,
    price           NUMERIC(12,2) NOT NULL CHECK (price >= 0),
    stock_quantity  INTEGER       NOT NULL CHECK (stock_quantity >= 0),
    active          BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE TABLE customers (
    id              BIGSERIAL PRIMARY KEY,
    legal_name      VARCHAR(150)  NOT NULL,
    trade_name      VARCHAR(150),
    document        VARCHAR(14)   NOT NULL UNIQUE,
    email           VARCHAR(120)  NOT NULL UNIQUE,
    phone           VARCHAR(20),
    city            VARCHAR(80)   NOT NULL,
    state           VARCHAR(2)    NOT NULL,
    tier            VARCHAR(20)   NOT NULL DEFAULT 'STANDARD'
                    CHECK (tier IN ('STANDARD', 'SILVER', 'GOLD', 'PLATINUM')),
    credit_limit    NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (credit_limit >= 0),
    billing_day     INTEGER       CHECK (billing_day BETWEEN 1 AND 31),
    active          BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE TABLE coupons (
    id               BIGSERIAL PRIMARY KEY,
    code             VARCHAR(30)   NOT NULL UNIQUE,
    description      VARCHAR(255),
    discount_percent NUMERIC(5,2)  NOT NULL CHECK (discount_percent > 0 AND discount_percent <= 50),
    valid_from       DATE          NOT NULL,
    valid_until      DATE          NOT NULL,
    max_uses         INTEGER       NOT NULL CHECK (max_uses > 0),
    used_count       INTEGER       NOT NULL DEFAULT 0,
    min_order_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    active           BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_coupon_period CHECK (valid_until >= valid_from)
);

CREATE TABLE orders (
    id                  BIGSERIAL PRIMARY KEY,
    customer_id         BIGINT        NOT NULL REFERENCES customers (id),
    coupon_id           BIGINT        REFERENCES coupons (id),
    status              VARCHAR(20)   NOT NULL
                        CHECK (status IN ('PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    subtotal            NUMERIC(12,2) NOT NULL,
    tier_discount       NUMERIC(12,2) NOT NULL DEFAULT 0,
    coupon_discount     NUMERIC(12,2) NOT NULL DEFAULT 0,
    shipping_fee        NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_amount        NUMERIC(12,2) NOT NULL,
    installments        INTEGER       NOT NULL DEFAULT 1 CHECK (installments BETWEEN 1 AND 6),
    notes               VARCHAR(500),
    created_at          TIMESTAMP     NOT NULL,
    confirmed_at        TIMESTAMP,
    shipped_at          TIMESTAMP,
    delivered_at        TIMESTAMP,
    cancelled_at        TIMESTAMP,
    cancellation_reason VARCHAR(255)
);

CREATE TABLE order_items (
    id          BIGSERIAL PRIMARY KEY,
    order_id    BIGINT        NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id  BIGINT        NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    quantity    INTEGER       NOT NULL,
    unit_price  NUMERIC(12,2) NOT NULL,
    line_total  NUMERIC(12,2) NOT NULL
);

CREATE SEQUENCE invoice_number_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE invoices (
    id            BIGSERIAL PRIMARY KEY,
    number        VARCHAR(20)   NOT NULL UNIQUE,
    order_id      BIGINT        NOT NULL UNIQUE REFERENCES orders (id),
    customer_id   BIGINT        NOT NULL REFERENCES customers (id),
    issue_date    DATE          NOT NULL,
    total_amount  NUMERIC(12,2) NOT NULL,
    status        VARCHAR(20)   NOT NULL CHECK (status IN ('OPEN', 'PAID', 'OVERDUE', 'CANCELLED')),
    created_at    TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE TABLE installments (
    id           BIGSERIAL PRIMARY KEY,
    invoice_id   BIGINT        NOT NULL REFERENCES invoices (id),
    number       INTEGER       NOT NULL,
    due_date     DATE          NOT NULL,
    amount       NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    status       VARCHAR(20)   NOT NULL CHECK (status IN ('OPEN', 'PAID', 'OVERDUE', 'CANCELLED')),
    paid_at      DATE,
    paid_amount  NUMERIC(12,2),
    late_fee     NUMERIC(12,2),
    interest     NUMERIC(12,2),
    CONSTRAINT uk_installment_invoice_number UNIQUE (invoice_id, number)
);

CREATE TABLE payments (
    id              BIGSERIAL PRIMARY KEY,
    installment_id  BIGINT        NOT NULL REFERENCES installments (id),
    amount          NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    payment_date    DATE          NOT NULL,
    method          VARCHAR(20)   NOT NULL CHECK (method IN ('PIX', 'BOLETO', 'TRANSFER')),
    registered_by   VARCHAR(50)   NOT NULL,
    created_at      TIMESTAMP     NOT NULL DEFAULT now()
);

-- Índices
CREATE INDEX idx_products_category     ON products (category_id);
CREATE INDEX idx_orders_status         ON orders (status);
CREATE INDEX idx_orders_created_at     ON orders (created_at);
CREATE INDEX idx_order_items_order     ON order_items (order_id);
CREATE INDEX idx_invoices_customer     ON invoices (customer_id);
CREATE INDEX idx_payments_installment  ON payments (installment_id);
