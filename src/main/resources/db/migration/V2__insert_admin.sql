INSERT INTO users (id, name, email, password, role)
VALUES (
    gen_random_uuid(),
    'Administrador',
    'admin@orbit.com',
    '$2a$12$0GZLoQCLzcINWoWSmIRr2.pTbEdBW97LANTm5/z6QRINCu3Y1K7xi',
    'ADMIN'
);