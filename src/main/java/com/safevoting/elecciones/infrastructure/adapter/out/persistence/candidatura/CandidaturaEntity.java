package com.safevoting.elecciones.infrastructure.adapter.out.persistence.candidatura;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("candidaturas")
public class CandidaturaEntity {

    @Id
    private UUID id;

    @Column("miembro_partido_id")
    private UUID miembroPartidoId;

    @Column("partido_id")
    private UUID partidoId;

    @Column("votacion_id")
    private UUID votacionId;

    @Column("fecha_inscripcion")
    private Instant fechaInscripcion;

    private String estado;

    @Column("created_at")
    private LocalDateTime createdAt;
}
