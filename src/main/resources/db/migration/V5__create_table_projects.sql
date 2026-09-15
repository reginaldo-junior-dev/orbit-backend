CREATE TABLE projects (
    id UUID PRIMARY KEY,
    name VARCHAR NOT NULL,
    description TEXT,
    status VARCHAR NOT NULL,
    created_at TIMESTAMP NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id)
);

CREATE INDEX idx_projects_user_id ON projects(user_id);
