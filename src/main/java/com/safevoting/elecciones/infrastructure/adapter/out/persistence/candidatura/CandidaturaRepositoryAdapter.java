package com.safevoting.elecciones.infrastructure.adapter.out.persistence.candidatura;

import com.safevoting.elecciones.application.candidatura.TarjetonItem;
import com.safevoting.elecciones.domain.model.candidatura.Candidatura;
import com.safevoting.elecciones.domain.model.votacion.AlcanceVotacion;
import com.safevoting.elecciones.domain.model.votacion.EstadoVotacion;
import com.safevoting.elecciones.domain.model.votacion.TipoVotacion;
import com.safevoting.elecciones.domain.model.votacion.Votacion;
import com.safevoting.elecciones.domain.repository.CandidaturaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CandidaturaRepositoryAdapter implements CandidaturaRepository {

    private final CandidaturaReactiveRepository repository;
    private final DatabaseClient databaseClient;
    private final CandidaturaPersistenceMapper mapper;

    @Override
    public Mono<Candidatura> save(Candidatura candidatura) {
        return repository.save(mapper.toEntity(candidatura)).map(mapper::toDomain);
    }

    @Override
    public Mono<Candidatura> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Flux<Candidatura> findActivasByVotacionId(UUID votacionId) {
        return repository.findByVotacionIdAndEstado(votacionId, "ACTIVA").map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countActivasByVotacionId(UUID votacionId) {
        return repository.countActivasByVotacionId(votacionId);
    }

    @Override
    public Flux<Candidatura> findActivasByPartidoId(UUID partidoId) {
        return repository.findByPartidoIdAndEstado(partidoId, "ACTIVA").map(mapper::toDomain);
    }

    @Override
    public Flux<Candidatura> findActivasByMiembroId(UUID miembroPartidoId) {
        return repository.findByMiembroPartidoIdAndEstado(miembroPartidoId, "ACTIVA").map(mapper::toDomain);
    }

    @Override
    public Mono<Long> cancelarByPartidoId(UUID partidoId) {
        return databaseClient.sql("UPDATE candidaturas SET estado = 'CANCELADA' WHERE partido_id = :partidoId AND estado = 'ACTIVA'")
                .bind("partidoId", partidoId)
                .fetch()
                .rowsUpdated();
    }

    @Override
    public Mono<Candidatura> update(Candidatura candidatura) {
        return databaseClient.sql("UPDATE candidaturas SET estado = :estado WHERE id = :id")
                .bind("id", candidatura.getId())
                .bind("estado", candidatura.getEstado().name())
                .fetch()
                .rowsUpdated()
                .thenReturn(candidatura);
    }

    @Override
    public Mono<Votacion> findVotacionByCandidaturaId(UUID candidaturaId) {
        return databaseClient.sql("""
                        SELECT v.id, v.nombre, v.tipo, v.alcance, v.departamento_id,
                               v.municipio_id, v.fecha_inicio, v.fecha_fin, v.estado, v.motivo
                        FROM votaciones v
                        JOIN candidaturas c ON c.votacion_id = v.id
                        WHERE c.id = :candidaturaId
                        """)
                .bind("candidaturaId", candidaturaId)
                .map((row, meta) -> Votacion.builder()
                        .id(row.get("id", UUID.class))
                        .nombre(row.get("nombre", String.class))
                        .tipo(TipoVotacion.valueOf(row.get("tipo", String.class)))
                        .alcance(AlcanceVotacion.valueOf(row.get("alcance", String.class)))
                        .estado(EstadoVotacion.valueOf(row.get("estado", String.class)))
                        .departamentoId(row.get("departamento_id", UUID.class))
                        .municipioId(row.get("municipio_id", UUID.class))
                        .fechaInicio(row.get("fecha_inicio", Instant.class))
                        .fechaFin(row.get("fecha_fin", Instant.class))
                        .motivo(row.get("motivo", String.class))
                        .build())
                .one();
    }

    @Override
    public Flux<TarjetonItem> findTarjetonByVotacionId(UUID votacionId) {
        return databaseClient.sql("""
                        SELECT c.id, c.estado, c.fecha_inscripcion,
                               mp.nombre_completo, mp.documento_identidad, mp.foto_url,
                               p.nombre AS nombre_partido, p.logo_url AS logo_partido,
                               c.miembro_partido_id, c.votacion_id
                        FROM candidaturas c
                        JOIN miembros_partidos mp ON c.miembro_partido_id = mp.id
                        JOIN partidos p ON c.partido_id = p.id
                        WHERE c.votacion_id = :votacionId AND c.estado = 'ACTIVA'
                        ORDER BY c.fecha_inscripcion ASC
                        """)
                .bind("votacionId", votacionId)
                .map((row, meta) -> new TarjetonItem(
                        row.get("id", UUID.class),
                        row.get("miembro_partido_id", UUID.class),
                        row.get("nombre_completo", String.class),
                        row.get("documento_identidad", String.class),
                        row.get("nombre_partido", String.class),
                        row.get("logo_partido", String.class),
                        row.get("foto_url", String.class),
                        row.get("votacion_id", UUID.class),
                        row.get("fecha_inscripcion", Instant.class),
                        row.get("estado", String.class)
                ))
                .all();
    }

    @Override
    public Mono<Candidatura> findByVotacionIdAndCandidaturaId(UUID votacionId, UUID candidaturaId) {
        return repository.findById(candidaturaId)
                .filter(e -> e.getVotacionId().equals(votacionId))
                .map(mapper::toDomain);
    }
}
