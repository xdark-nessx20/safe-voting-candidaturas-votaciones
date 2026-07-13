package com.safevoting.elecciones.infrastructure.adapter.out.persistence.miembro;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("miembros_partidos")
public class MiembroEntity {

    @Id
    private UUID id;

    @Column("usuario_id")
    private UUID usuarioId;

    @Column("partido_id")
    private UUID partidoId;

    @Column("nombre_completo")
    private String nombreCompleto;

    @Column("documento_identidad")
    private String documentoIdentidad;

    @Column("lugar_inscripcion")
    private String lugarInscripcion;

    @Column("foto_url")
    private String fotoUrl;

    private String estado;

    private boolean verificado;

    @Column("motivo_baja")
    private String motivoBaja;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
