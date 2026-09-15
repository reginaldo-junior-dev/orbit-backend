INSERT INTO users (id, name, email, password, role)
VALUES (
    gen_random_uuid(),
    'Lucas',
    'user@orbit.com',
    '$2a$12$whHmW5BpoC4AK/ys3RiWU.YgODuwZ.hWfLe/drTOn/Byqe/xZmxe2',
    'USER'
);