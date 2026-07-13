package com.safevoting.elecciones.infrastructure.adapter.out.persistence.votacion;

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
@Table("votaciones")
public class VotacionEntity {

    @Id
    private UUID id;
    private String nombre;
    private String tipo;
    private String alcance;

    @Column("departamento_id")
    private UUID departamentoId;

    @Column("municipio_id")
    private UUID municipioId;

    @Column("fecha_inicio")
    private Instant fechaInicio;

    @Column("fecha_fin")
    private Instant fechaFin;
    private String estado;
    private String motivo;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
