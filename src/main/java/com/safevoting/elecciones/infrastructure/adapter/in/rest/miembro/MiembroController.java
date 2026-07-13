package com.safevoting.elecciones.infrastructure.adapter.in.rest.miembro;

import com.safevoting.elecciones.application.miembro.CrearMiembroUseCase;
import com.safevoting.elecciones.application.miembro.DarBajaMiembroUseCase;
import com.safevoting.elecciones.application.miembro.EditarMiembroUseCase;
import com.safevoting.elecciones.application.miembro.ListarMiembrosUseCase;
import com.safevoting.elecciones.application.miembro.ObtenerMiembroUseCase;
import com.safevoting.elecciones.domain.exception.miembro.UsuarioNoEncontradoException;
import com.safevoting.elecciones.domain.model.partido.MiembroPartido;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.miembro.dto.BajaRequest;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.miembro.dto.MiembroRequest;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.miembro.dto.MiembroResponse;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.miembro.mapper.MiembroDtoMapper;
import com.safevoting.elecciones.infrastructure.adapter.out.http.UsuarioServiceClient;
import com.safevoting.elecciones.infrastructure.adapter.out.http.dto.UsuarioResponse;
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
@RequestMapping("/api/v1/partidos/{partidoId}/miembros")
@RequiredArgsConstructor
@Tag(name = "Miembros de Partido")
public class MiembroController {

    private final CrearMiembroUseCase crearMiembroUseCase;
    private final ListarMiembrosUseCase listarMiembrosUseCase;
    private final ObtenerMiembroUseCase obtenerMiembroUseCase;
    private final EditarMiembroUseCase editarMiembroUseCase;
    private final DarBajaMiembroUseCase darBajaMiembroUseCase;
    private final UsuarioServiceClient usuarioServiceClient;
    private final MiembroDtoMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MiembroResponse> crear(@PathVariable UUID partidoId, @Valid @RequestBody MiembroRequest request) {
        return usuarioServiceClient.getUsuario(request.usuarioId())
                .filter(usr -> "HABILITADO".equals(usr.estado()))
                .map(this::buildMiembroVerificado)
                .switchIfEmpty(Mono.defer(() -> construirMiembroTentativo(request)))
                .flatMap(miembro -> crearMiembroUseCase.ejecutar(partidoId, miembro, request.fotoBase64()))
                .map(mapper::toResponse);
    }

    @GetMapping
    public Flux<MiembroResponse> listar(@PathVariable UUID partidoId) {
        return listarMiembrosUseCase.ejecutar(partidoId)
                .map(mapper::toResponse);
    }

    @GetMapping("/{id}")
    public Mono<MiembroResponse> obtenerPorId(@PathVariable UUID partidoId, @PathVariable UUID id) {
        return obtenerMiembroUseCase.ejecutar(id)
                .map(mapper::toResponse);
    }

    @PutMapping("/{id}")
    public Mono<MiembroResponse> editar(@PathVariable UUID partidoId, @PathVariable UUID id, @Valid @RequestBody MiembroRequest request) {
        return editarMiembroUseCase.ejecutar(id, request.fotoBase64())
                .map(mapper::toResponse);
    }

    @PatchMapping("/{id}/baja")
    public Mono<MiembroResponse> darBaja(@PathVariable UUID partidoId, @PathVariable UUID id, @RequestBody BajaRequest request) {
        String motivo = request.motivo() != null ? request.motivo() : "BAJA_ADMINISTRATIVA";
        return darBajaMiembroUseCase.ejecutar(id, motivo)
                .map(mapper::toResponse);
    }

    private MiembroPartido buildMiembroVerificado(UsuarioResponse usuario) {
        MiembroPartido miembro = mapper.toDomain(usuario);
        miembro.marcarVerificado();
        return miembro;
    }

    private Mono<MiembroPartido> construirMiembroTentativo(MiembroRequest request) {
        MiembroPartido miembro = mapper.toDomain(request);
        if (isBlank(miembro.getNombreCompleto()) || isBlank(miembro.getDocumentoIdentidad()) || isBlank(miembro.getLugarInscripcion())) {
            return Mono.error(new UsuarioNoEncontradoException(request.usuarioId()));
        }
        return Mono.just(miembro);
    }

    private boolean isBlank(String str) {
        return str == null || str.isBlank();
    }
}
