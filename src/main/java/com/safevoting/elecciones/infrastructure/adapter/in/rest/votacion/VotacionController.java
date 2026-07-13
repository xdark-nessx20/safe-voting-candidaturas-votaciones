package com.safevoting.elecciones.infrastructure.adapter.in.rest.votacion;

import com.safevoting.elecciones.application.votacion.AbrirVotacionUseCase;
import com.safevoting.elecciones.application.votacion.CancelarVotacionUseCase;
import com.safevoting.elecciones.application.votacion.CerrarVotacionUseCase;
import com.safevoting.elecciones.application.votacion.CompletarVotacionUseCase;
import com.safevoting.elecciones.application.votacion.CrearVotacionUseCase;
import com.safevoting.elecciones.application.votacion.EstablecerFechasUseCase;
import com.safevoting.elecciones.application.votacion.ListarVotacionesUseCase;
import com.safevoting.elecciones.application.votacion.ObtenerVotacionUseCase;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.votacion.dto.FechasRequest;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.votacion.dto.MotivoRequest;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.votacion.dto.VotacionRequest;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.votacion.dto.VotacionResponse;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.votacion.mapper.VotacionDtoMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/votaciones")
@RequiredArgsConstructor
@Tag(name = "Votaciones")
public class VotacionController {

    private final CrearVotacionUseCase crearVotacionUseCase;
    private final EstablecerFechasUseCase establecerFechasUseCase;
    private final AbrirVotacionUseCase abrirVotacionUseCase;
    private final CerrarVotacionUseCase cerrarVotacionUseCase;
    private final CancelarVotacionUseCase cancelarVotacionUseCase;
    private final CompletarVotacionUseCase completarVotacionUseCase;
    private final ObtenerVotacionUseCase obtenerVotacionUseCase;
    private final ListarVotacionesUseCase listarVotacionesUseCase;
    private final VotacionDtoMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<VotacionResponse> crear(@Valid @RequestBody VotacionRequest request) {
        return crearVotacionUseCase.ejecutar(mapper.toDomain(request))
                .map(mapper::toResponse);
    }

    @GetMapping
    public Flux<VotacionResponse> listar() {
        return listarVotacionesUseCase.ejecutar()
                .map(mapper::toResponse);
    }

    @GetMapping("/{id}")
    public Mono<VotacionResponse> obtenerPorId(@PathVariable UUID id) {
        return obtenerVotacionUseCase.ejecutar(id)
                .map(mapper::toResponse);
    }

    @PatchMapping("/{id}/fechas")
    public Mono<VotacionResponse> establecerFechas(@PathVariable UUID id, @Valid @RequestBody FechasRequest request) {
        return establecerFechasUseCase.ejecutar(id, request.fechaInicio(), request.fechaFin())
                .map(mapper::toResponse);
    }

    @PatchMapping("/{id}/abrir")
    public Mono<VotacionResponse> abrir(@PathVariable UUID id) {
        return abrirVotacionUseCase.ejecutar(id)
                .map(mapper::toResponse);
    }

    @PatchMapping("/{id}/cerrar")
    public Mono<VotacionResponse> cerrar(@PathVariable UUID id) {
        return cerrarVotacionUseCase.ejecutar(id)
                .map(mapper::toResponse);
    }

    @PatchMapping("/{id}/cancelar")
    public Mono<VotacionResponse> cancelar(@PathVariable UUID id, @Valid @RequestBody MotivoRequest request) {
        return cancelarVotacionUseCase.ejecutar(id, request.motivo())
                .map(mapper::toResponse);
    }

    @PatchMapping("/{id}/completar")
    public Mono<VotacionResponse> completar(@PathVariable UUID id) {
        return completarVotacionUseCase.ejecutar(id)
                .map(mapper::toResponse);
    }
}
