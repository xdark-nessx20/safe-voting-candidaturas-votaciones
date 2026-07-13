package com.safevoting.elecciones.infrastructure.adapter.out.persistence.miembro;

import com.safevoting.elecciones.domain.model.partido.MiembroPartido;
import com.safevoting.elecciones.domain.repository.MiembroPartidoRepository;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MiembroRepositoryAdapter implements MiembroPartidoRepository {

    private final MiembroReactiveRepository repository;
    private final DatabaseClient databaseClient;
    private final MiembroPersistenceMapper mapper;

    @Override
    public Mono<MiembroPartido> save(MiembroPartido miembro) {
        return repository.save(mapper.toEntity(miembro)).map(mapper::toDomain);
    }

    @Override
    public Mono<MiembroPartido> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Flux<MiembroPartido> findByPartidoId(UUID partidoId) {
        return databaseClient.sql("SELECT * FROM miembros_partidos WHERE partido_id = :partidoId")
                .bind("partidoId", partidoId)
                .map(this::rowToDomain)
                .all();
    }

    @Override
    public Flux<MiembroPartido> findByUsuarioId(UUID usuarioId) {
        return databaseClient.sql("SELECT * FROM miembros_partidos WHERE usuario_id = :usuarioId")
                .bind("usuarioId", usuarioId)
                .map(this::rowToDomain)
                .all();
    }

    @Override
    public Mono<Boolean> existsByUsuarioIdAndPartidoId(UUID usuarioId, UUID partidoId) {
        return databaseClient.sql("SELECT EXISTS(SELECT 1 FROM miembro_partido WHERE usuario_id = :usuarioId AND partido_id = :partidoId)")
                .bind("usuarioId", usuarioId)
                .bind("partidoId", partidoId)
                .mapValue(Boolean.class)
                .one()
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<MiembroPartido> update(MiembroPartido miembro) {
        return databaseClient.sql("UPDATE miembros_partidos SET foto_url = :fotoUrl, estado = :estado, motivo_baja = :motivoBaja, verificado = :verificado, nombre_completo = :nombreCompleto, documento_identidad = :documentoIdentidad, lugar_inscripcion = :lugarInscripcion, updated_at = NOW() WHERE id = :id")
                .bind("id", miembro.getId())
                .bind("fotoUrl", miembro.getFotoUrl())
                .bind("estado", miembro.getEstado().name())
                .bind("motivoBaja", miembro.getMotivoBaja())
                .bind("verificado", miembro.isVerificado())
                .bind("nombreCompleto", miembro.getNombreCompleto())
                .bind("documentoIdentidad", miembro.getDocumentoIdentidad())
                .bind("lugarInscripcion", miembro.getLugarInscripcion())
                .fetch()
                .rowsUpdated()
                .thenReturn(miembro);
    }

    private MiembroPartido rowToDomain(Row row, RowMetadata metadata) {
        return mapper.toDomain(MiembroEntity.builder()
                .id(row.get("id", UUID.class))
                .usuarioId(row.get("usuario_id", UUID.class))
                .partidoId(row.get("partido_id", UUID.class))
                .nombreCompleto(row.get("nombre_completo", String.class))
                .documentoIdentidad(row.get("documento_identidad", String.class))
                .lugarInscripcion(row.get("lugar_inscripcion", String.class))
                .fotoUrl(row.get("foto_url", String.class))
                .estado(row.get("estado", String.class))
                .verificado(Boolean.TRUE.equals(row.get("verificado", Boolean.class)))
                .motivoBaja(row.get("motivo_baja", String.class))
                .createdAt(row.get("created_at", LocalDateTime.class))
                .updatedAt(row.get("updated_at", LocalDateTime.class))
                .build());
    }
}
