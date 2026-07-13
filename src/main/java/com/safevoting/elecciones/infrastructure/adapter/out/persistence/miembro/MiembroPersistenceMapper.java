package com.safevoting.elecciones.infrastructure.adapter.out.persistence.miembro;

import com.safevoting.elecciones.domain.model.partido.MiembroPartido;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MiembroPersistenceMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MiembroEntity toEntity(MiembroPartido domain);

    MiembroPartido toDomain(MiembroEntity entity);
}
