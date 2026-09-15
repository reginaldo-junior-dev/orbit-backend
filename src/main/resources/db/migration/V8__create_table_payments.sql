CREATE TABLE payments (
    id UUID PRIMARY KEY,
    plan VARCHAR NOT NULL,
    billing_cycle VARCHAR NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    status VARCHAR NOT NULL,
    preference_id VARCHAR,
    provider_payment_id VARCHAR,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id)
);

CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_provider_payment_id ON payments(provider_payment_id);
