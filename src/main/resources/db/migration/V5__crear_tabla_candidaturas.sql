CREATE TABLE candidaturas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    miembro_partido_id UUID NOT NULL REFERENCES miembros_partidos(id),
    partido_id UUID NOT NULL REFERENCES partidos(id),
    votacion_id UUID NOT NULL REFERENCES votaciones(id),
    fecha_inscripcion TIMESTAMP NOT NULL DEFAULT NOW(),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_estado_candidatura CHECK (estado IN ('ACTIVA','CANCELADA')),
    CONSTRAINT uq_miembro_votacion UNIQUE (miembro_partido_id, votacion_id)
);

CREATE INDEX idx_candidaturas_votacion_estado ON candidaturas (votacion_id, estado);
CREATE INDEX idx_candidaturas_miembro ON candidaturas (miembro_partido_id);
CREATE INDEX idx_candidaturas_partido_estado ON candidaturas (partido_id, estado);
