CREATE TABLE partidos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    logo_url VARCHAR(500),
    estado VARCHAR(20) NOT NULL DEFAULT 'HABILITADO',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_estado_partido CHECK (estado IN ('HABILITADO','INHABILITADO')),
    CONSTRAINT chk_nombre_uppercase CHECK (nombre ~ '^[A-Z ]+$')
);
