package com.safevoting.elecciones.infrastructure.adapter.out.persistence.partido;

import com.safevoting.elecciones.domain.model.partido.PartidoPolitico;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PartidoPersistenceMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PartidoEntity toEntity(PartidoPolitico domain);

    PartidoPolitico toDomain(PartidoEntity entity);
}
