package com.safevoting.elecciones.infrastructure.adapter.in.rest.participacion;

import com.safevoting.elecciones.application.voto.ConsultarHistorialUseCase;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.participacion.dto.HistorialResponse;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.participacion.mapper.ParticipacionMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/participaciones")
@RequiredArgsConstructor
@Tag(name = "Participaciones")
public class ParticipacionController {

    private final ConsultarHistorialUseCase consultarHistorialUseCase;
    private final ParticipacionMapper mapper;

    @GetMapping("/mis-votos")
    public Flux<HistorialResponse> misVotos() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> UUID.fromString(ctx.getAuthentication().getName()))
                .flatMapMany(usuarioId -> consultarHistorialUseCase.ejecutar(usuarioId)
                        .map(mapper::toResponse));
    }
}
