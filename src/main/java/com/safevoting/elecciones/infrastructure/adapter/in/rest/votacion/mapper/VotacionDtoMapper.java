package com.safevoting.elecciones.infrastructure.adapter.in.rest.votacion.mapper;

import com.safevoting.elecciones.domain.model.votacion.Votacion;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.votacion.dto.VotacionRequest;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.votacion.dto.VotacionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VotacionDtoMapper {

    VotacionResponse toResponse(Votacion votacion);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "alcance", expression = "java(request.tipo().alcanceCompatible())")
    @Mapping(target = "fechaInicio", ignore = true)
    @Mapping(target = "fechaFin", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "motivo", ignore = true)
    Votacion toDomain(VotacionRequest request);
}
