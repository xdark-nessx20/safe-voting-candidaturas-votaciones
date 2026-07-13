CREATE TABLE votos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    votacion_id UUID NOT NULL REFERENCES votaciones(id),
    candidatura_id UUID NOT NULL REFERENCES candidaturas(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_votos_votacion ON votos (votacion_id);
