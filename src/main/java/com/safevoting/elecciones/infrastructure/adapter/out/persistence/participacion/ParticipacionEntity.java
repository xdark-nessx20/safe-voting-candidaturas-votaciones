package com.safevoting.elecciones.infrastructure.adapter.out.persistence.participacion;

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
@Table("participaciones")
public class ParticipacionEntity {

    @Id
    private UUID id;

    @Column("usuario_id")
    private UUID usuarioId;

    @Column("voto_id")
    private UUID votoId;

    @Column("votacion_id")
    private UUID votacionId;
    private String estado;

    @Column("fecha_emision")
    private Instant fechaEmision;
}
