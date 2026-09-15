CREATE TABLE goals (
    id UUID PRIMARY KEY,
    title VARCHAR NOT NULL,
    description TEXT,
    status VARCHAR NOT NULL,
    target_date DATE,
    created_at TIMESTAMP NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id)
);

CREATE INDEX idx_goals_user_id ON goals(user_id);
