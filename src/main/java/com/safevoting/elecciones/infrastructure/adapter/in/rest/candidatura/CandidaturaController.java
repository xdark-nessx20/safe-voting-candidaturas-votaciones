package com.safevoting.elecciones.infrastructure.adapter.in.rest.candidatura;

import com.safevoting.elecciones.application.candidatura.CancelarCandidaturaUseCase;
import com.safevoting.elecciones.application.candidatura.InscribirCandidaturaUseCase;
import com.safevoting.elecciones.application.candidatura.ListarCandidaturasPorVotacionUseCase;
import com.safevoting.elecciones.domain.model.candidatura.Candidatura;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.candidatura.dto.InscripcionRequest;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.candidatura.dto.TarjetonResponse;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.candidatura.mapper.CandidaturaDtoMapper;
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
@RequiredArgsConstructor
@Tag(name = "Candidaturas")
public class CandidaturaController {

    private final InscribirCandidaturaUseCase inscribirCandidaturaUseCase;
    private final CancelarCandidaturaUseCase cancelarCandidaturaUseCase;
    private final ListarCandidaturasPorVotacionUseCase listarCandidaturasPorVotacionUseCase;
    private final CandidaturaDtoMapper mapper;

    @PostMapping("/api/v1/candidaturas")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TarjetonResponse> inscribir(@Valid @RequestBody InscripcionRequest request) {
        return inscribirCandidaturaUseCase.ejecutar(request.miembroPartidoId(), request.votacionId())
                .map(mapper::toResponse);
    }

    @PatchMapping("/api/v1/candidaturas/{id}/cancelar")
    public Mono<TarjetonResponse> cancelar(@PathVariable UUID id) {
        return cancelarCandidaturaUseCase.ejecutar(id)
                .map(mapper::toResponse);
    }

    @GetMapping("/api/v1/votaciones/{votacionId}/tarjeton")
    public Flux<TarjetonResponse> tarjeton(@PathVariable UUID votacionId) {
        return listarCandidaturasPorVotacionUseCase.ejecutar(votacionId)
                .map(mapper::toResponse);
    }
}
