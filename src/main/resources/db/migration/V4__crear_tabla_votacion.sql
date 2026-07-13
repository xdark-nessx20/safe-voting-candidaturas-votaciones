CREATE TABLE votaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(200) NOT NULL UNIQUE,
    tipo VARCHAR(20) NOT NULL,
    alcance VARCHAR(20) NOT NULL,
    departamento_id UUID,
    municipio_id UUID,
    fecha_inicio TIMESTAMP,
    fecha_fin TIMESTAMP,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    motivo TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_estado_votacion CHECK (estado IN ('ACTIVA','EN_PROGRESO','FINALIZADA','CANCELADA','COMPLETADA')),
    CONSTRAINT chk_tipo_votacion CHECK (tipo IN ('PRESIDENCIA','CONGRESO','ALCALDIA','GOBERNACION')),
    CONSTRAINT chk_alcance_votacion CHECK (alcance IN ('MUNICIPAL','DEPARTAMENTAL','REGIONAL','NACIONAL'))
);
