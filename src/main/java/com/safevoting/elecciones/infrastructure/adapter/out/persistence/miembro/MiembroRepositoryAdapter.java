package com.safevoting.elecciones.infrastructure.adapter.out.persistence.miembro;

import com.safevoting.elecciones.domain.model.partido.MiembroPartido;
import com.safevoting.elecciones.domain.repository.MiembroPartidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
        return repository.findByPartidoId(partidoId).map(mapper::toDomain);
    }

    @Override
    public Flux<MiembroPartido> findByUsuarioId(UUID usuarioId) {
        return repository.findByUsuarioId(usuarioId).map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByUsuarioIdAndPartidoId(UUID usuarioId, UUID partidoId) {
        return repository.existsByUsuarioIdAndPartidoId(usuarioId, partidoId);
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
}
