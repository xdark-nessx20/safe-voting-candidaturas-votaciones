package com.safevoting.elecciones.infrastructure.adapter.out.persistence.partido;

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
@Table("partidos")
public class PartidoEntity {

    @Id
    private UUID id;
    private String nombre;
    private String descripcion;

    @Column("logo_url")
    private String logoUrl;
    private String estado;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
