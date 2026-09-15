CREATE TABLE tasks (
    id UUID PRIMARY KEY,
    title VARCHAR NOT NULL,
    description TEXT,
    status VARCHAR NOT NULL,
    priority VARCHAR NOT NULL,
    due_date DATE,
    created_at TIMESTAMP NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id)
);

CREATE INDEX idx_tasks_user_id ON tasks(user_id);
