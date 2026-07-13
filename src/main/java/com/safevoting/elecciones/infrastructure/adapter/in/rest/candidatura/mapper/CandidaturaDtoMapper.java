package com.safevoting.elecciones.infrastructure.adapter.in.rest.candidatura.mapper;

import com.safevoting.elecciones.application.candidatura.TarjetonItem;
import com.safevoting.elecciones.domain.model.candidatura.Candidatura;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.candidatura.dto.TarjetonResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CandidaturaDtoMapper {

    @Mapping(target = "nombreCandidato", ignore = true)
    @Mapping(target = "documentoIdentidad", ignore = true)
    @Mapping(target = "nombrePartido", ignore = true)
    @Mapping(target = "logoPartido", ignore = true)
    @Mapping(target = "fotoUrl", ignore = true)
    TarjetonResponse toResponse(Candidatura candidatura);

    TarjetonResponse toResponse(TarjetonItem item);
}
