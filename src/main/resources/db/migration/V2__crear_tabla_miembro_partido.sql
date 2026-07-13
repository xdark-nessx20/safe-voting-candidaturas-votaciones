CREATE TABLE miembros_partidos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    partido_id UUID NOT NULL REFERENCES partidos(id),
    nombre_completo VARCHAR(200) NOT NULL,
    documento_identidad VARCHAR(50) NOT NULL,
    lugar_inscripcion VARCHAR(200) NOT NULL,
    foto_url VARCHAR(500),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    verificado BOOLEAN NOT NULL DEFAULT FALSE,
    motivo_baja VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_estado_miembro CHECK (estado IN ('ACTIVO','INACTIVO')),
    CONSTRAINT uq_miembro_usuario_partido UNIQUE (usuario_id, partido_id)
);
