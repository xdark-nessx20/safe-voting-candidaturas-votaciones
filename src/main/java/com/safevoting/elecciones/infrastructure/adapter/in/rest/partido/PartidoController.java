package com.safevoting.elecciones.infrastructure.adapter.in.rest.partido;

import com.safevoting.elecciones.application.partido.CrearPartidoUseCase;
import com.safevoting.elecciones.application.partido.EditarPartidoUseCase;
import com.safevoting.elecciones.application.partido.InhabilitarPartidoUseCase;
import com.safevoting.elecciones.application.partido.ListarPartidosUseCase;
import com.safevoting.elecciones.application.partido.ObtenerPartidoUseCase;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.partido.dto.PartidoRequest;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.partido.dto.PartidoResponse;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.partido.mapper.PartidoDtoMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/partidos")
@RequiredArgsConstructor
@Tag(name = "Partidos Políticos")
public class PartidoController {

    private final CrearPartidoUseCase crearPartidoUseCase;
    private final EditarPartidoUseCase editarPartidoUseCase;
    private final ListarPartidosUseCase listarPartidosUseCase;
    private final ObtenerPartidoUseCase obtenerPartidoUseCase;
    private final InhabilitarPartidoUseCase inhabilitarPartidoUseCase;
    private final PartidoDtoMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PartidoResponse> crear(@Valid @RequestBody PartidoRequest request) {
        return crearPartidoUseCase.ejecutar(mapper.toDomain(request), request.logoBase64())
                .map(mapper::toResponse);
    }

    @GetMapping
    public Flux<PartidoResponse> listar() {
        return listarPartidosUseCase.ejecutar()
                .map(mapper::toResponse);
    }

    @GetMapping("/{id}")
    public Mono<PartidoResponse> obtenerPorId(@PathVariable UUID id) {
        return obtenerPartidoUseCase.ejecutar(id)
                .map(mapper::toResponse);
    }

    @PutMapping("/{id}")
    public Mono<PartidoResponse> editar(@PathVariable UUID id, @Valid @RequestBody PartidoRequest request) {
        return editarPartidoUseCase.ejecutar(id, mapper.toDomain(request), request.logoBase64())
                .map(mapper::toResponse);
    }

    @PatchMapping("/{id}/inhabilitar")
    public Mono<PartidoResponse> inhabilitar(@PathVariable UUID id) {
        return inhabilitarPartidoUseCase.ejecutar(id)
                .map(mapper::toResponse);
    }
}
