package com.safevoting.elecciones.infrastructure.adapter.out.persistence.votacion;

import com.safevoting.elecciones.domain.model.votacion.Votacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VotacionPersistenceMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    VotacionEntity toEntity(Votacion domain);

    Votacion toDomain(VotacionEntity entity);
}
