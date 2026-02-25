-- Limpa as tabelas para evitar duplicidade (ordem correta: primeiro filha, depois pai)
DELETE FROM user_roles;
DELETE FROM users;

-- Insere o usuário de teste com senha 'senha123' (hash BCrypt válido)
INSERT INTO users (username, password) VALUES ('usuario', '$2a$10$Mp8HqO2C7SBunAw7g4h0eeNYLYkPMKSFVeJaFi4.W9crQp.WtzXIK');

-- Insere a role USER para o usuário (ID 1 gerado automaticamente)
INSERT INTO user_roles (user_id, roles) VALUES (1, 'USER');
