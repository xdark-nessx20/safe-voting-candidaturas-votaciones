package com.safevoting.elecciones.infrastructure.adapter.in.rest.partido.mapper;

import com.safevoting.elecciones.domain.model.partido.PartidoPolitico;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.partido.dto.PartidoRequest;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.partido.dto.PartidoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PartidoDtoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "logoUrl", ignore = true)
    @Mapping(target = "estado", ignore = true)
    PartidoPolitico toDomain(PartidoRequest request);

    PartidoResponse toResponse(PartidoPolitico partido);
}
