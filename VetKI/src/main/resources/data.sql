-- =============================================================
-- Fase 5: Datos Iniciales - VetKI
-- =============================================================
-- Usuarios de prueba para cada rol del sistema.
-- Contrasenas codificadas con BCrypt (strength=10).
--
--   ADMIN   -> admin@vetki.com     / Admin1234!
--   VET     -> vet@vetki.com       / Vet1234!
--   CLIENTE -> cliente@vetki.com   / Cliente1234!
-- =============================================================

INSERT INTO usuarios (nombre, telefono, email, password, rol)
VALUES
    (
        'Administrador VetKI',
        '50212345678',
        'admin@vetki.com',
        '$2a$10$Z4GEA18t0JGg8qFHKYKZFOtrpbdd.C.GrXTjR6rypVxrND8/6Xege',
        'ADMIN'
    ),
    (
        'Dr. Carlos Ramirez',
        '50287654321',
        'vet@vetki.com',
        '$2a$10$FecckwNjcc6478i4K24WAO02RLLe6uXplPY6aCHhLyQa2OEljjEAe',
        'VET'
    ),
    (
        'Maria Lopez',
        '50211223344',
        'cliente@vetki.com',
        '$2a$10$NREpdfVNzfYwQMynMaOhu.dmF6OZmXtN/yTC83rHxtSCthtKbXErC',
        'CLIENTE'
    )
ON CONFLICT (email) DO UPDATE SET password = EXCLUDED.password;