ALTER TABLE tasks ADD COLUMN project_id UUID REFERENCES projects(id);

CREATE INDEX idx_tasks_project_id ON tasks(project_id);
