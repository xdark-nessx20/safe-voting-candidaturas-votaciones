package com.safevoting.elecciones.infrastructure.adapter.in.rest.miembro.mapper;

import com.safevoting.elecciones.domain.model.partido.MiembroPartido;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.miembro.dto.MiembroRequest;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.miembro.dto.MiembroResponse;
import com.safevoting.elecciones.infrastructure.adapter.out.http.dto.UsuarioResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MiembroDtoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "partidoId", ignore = true)
    @Mapping(target = "fotoUrl", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "verificado", ignore = true)
    @Mapping(target = "motivoBaja", ignore = true)
    MiembroPartido toDomain(MiembroRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", source = "id")
    @Mapping(target = "partidoId", ignore = true)
    @Mapping(target = "fotoUrl", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "verificado", ignore = true)
    @Mapping(target = "motivoBaja", ignore = true)
    MiembroPartido toDomain(UsuarioResponse usuario);

    @Mapping(target = "nombrePartido", ignore = true)
    MiembroResponse toResponse(MiembroPartido miembro);
}
