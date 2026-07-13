CREATE TABLE participaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    voto_id UUID NOT NULL REFERENCES votos(id),
    votacion_id UUID NOT NULL REFERENCES votaciones(id),
    estado VARCHAR(20) NOT NULL DEFAULT 'VALIDO',
    fecha_emision TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_estado_participacion CHECK (estado IN ('VALIDO','ANULADO'))
);

CREATE UNIQUE INDEX idx_participacion_usuario_votacion ON participaciones (usuario_id, votacion_id);
