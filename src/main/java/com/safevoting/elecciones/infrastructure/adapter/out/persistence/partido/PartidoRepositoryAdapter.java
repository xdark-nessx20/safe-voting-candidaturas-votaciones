package com.safevoting.elecciones.infrastructure.adapter.out.persistence.partido;

import com.safevoting.elecciones.domain.model.partido.PartidoPolitico;
import com.safevoting.elecciones.domain.repository.PartidoPoliticoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PartidoRepositoryAdapter implements PartidoPoliticoRepository {

    private final PartidoReactiveRepository repository;
    private final DatabaseClient databaseClient;
    private final PartidoPersistenceMapper mapper;

    @Override
    public Mono<PartidoPolitico> save(PartidoPolitico partido) {
        return repository.save(mapper.toEntity(partido)).map(mapper::toDomain);
    }

    @Override
    public Mono<PartidoPolitico> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Flux<PartidoPolitico> findAll() {
        return repository.findAll().map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByNombre(String nombre) {
        return repository.existsByNombre(nombre);
    }

    @Override
    public Mono<PartidoPolitico> update(PartidoPolitico partido) {
        var spec = databaseClient.sql("""
                        UPDATE partidos
                        SET nombre = :nombre,
                            descripcion = :descripcion,
                            logo_url = :logoUrl,
                            estado = :estado,
                            updated_at = NOW()
                        WHERE id = :id
                        """)
                .bind("nombre", partido.getNombre())
                .bind("estado", partido.getEstado().name())
                .bind("id", partido.getId());

        if (partido.getDescripcion() != null) {
            spec = spec.bind("descripcion", partido.getDescripcion());
        } else {
            spec = spec.bindNull("descripcion", String.class);
        }

        if (partido.getLogoUrl() != null) {
            spec = spec.bind("logoUrl", partido.getLogoUrl());
        } else {
            spec = spec.bindNull("logoUrl", String.class);
        }

        return spec.then().thenReturn(partido);
    }
}
