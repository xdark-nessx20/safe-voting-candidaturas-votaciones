package com.safevoting.elecciones.infrastructure.adapter.out.persistence.voto;

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
@Table("votos")
public class VotoEntity {

    @Id
    private UUID id;

    @Column("votacion_id")
    private UUID votacionId;

    @Column("candidatura_id")
    private UUID candidaturaId;

    @Column("created_at")
    private LocalDateTime createdAt;
}
