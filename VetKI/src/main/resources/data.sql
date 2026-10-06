-- =============================================================
-- Fase 5: Datos Iniciales - VetKI
-- =============================================================
-- Usuarios de prueba para cada rol del sistema.
-- Las contrasenas estan codificadas con BCrypt (12 rounds).
--
--   ADMIN   -> admin@vetki.com     / Admin1234!
--   VET     -> vet@vetki.com       / Vet1234!
--   CLIENTE -> cliente@vetki.com   / Cliente1234!
--
-- Se usa INSERT ... ON CONFLICT DO NOTHING para evitar
-- duplicados en reinicios con ddl-auto=update.
-- =============================================================

INSERT INTO usuarios (nombre, telefono, email, password, rol)
VALUES
    (
        'Administrador VetKI',
        '50212345678',
        'admin@vetki.com',
        '$2a$12$Y6H0s7b6tRnlFwRqy2.nkuC8E/Ue5Kl2TqvUB9MVZvt7qsrXe0oi',
        'ADMIN'
    ),
    (
        'Dr. Carlos Ramirez',
        '50287654321',
        'vet@vetki.com',
        '$2a$12$AzqWzC0o5RL1N4e1xF0uLO.N4kmVJM0GNhfYXL0iP8B5kP.jAkB3u',
        'VET'
    ),
    (
        'Maria Lopez',
        '50211223344',
        'cliente@vetki.com',
        '$2a$12$mHZ5p0b9nSAMnNz0JRuLsOHGbzHwT5BU1r.AvPFm2QR6TkJrOgHKi',
        'CLIENTE'
    )
ON CONFLICT (email) DO NOTHING;
