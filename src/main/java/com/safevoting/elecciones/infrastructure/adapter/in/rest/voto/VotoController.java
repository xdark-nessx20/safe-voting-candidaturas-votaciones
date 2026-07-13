package com.safevoting.elecciones.infrastructure.adapter.in.rest.voto;

import com.safevoting.elecciones.application.voto.EmitirVotoUseCase;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.voto.dto.VotoRequest;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.voto.dto.VotoResponse;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.voto.mapper.VotoMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/votaciones/{votacionId}/votos")
@RequiredArgsConstructor
@Tag(name = "Votos")
public class VotoController {

    private final EmitirVotoUseCase emitirVotoUseCase;
    private final VotoMapper mapper;

    @PostMapping
    public Mono<VotoResponse> emitir(@PathVariable UUID votacionId, @Valid @RequestBody VotoRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> UUID.fromString(ctx.getAuthentication().getName()))
                .flatMap(usuarioId -> emitirVotoUseCase.ejecutar(usuarioId, votacionId, request.candidaturaId()))
                .map(mapper::toResponse);
    }
}
