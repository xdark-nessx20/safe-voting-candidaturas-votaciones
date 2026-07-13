package com.safevoting.elecciones.infrastructure.adapter.out.client;

import com.safevoting.elecciones.domain.model.votacion.AlcanceVotacion;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class Modulo1Client {

    private final WebClient webClient;

    public Modulo1Client(WebClient modulo1WebClient) {
        this.webClient = modulo1WebClient;
    }

    @CircuitBreaker(name = "modulo1", fallbackMethod = "fallbackGetAlcanceGestor")
    public Mono<AlcanceGestorResponse> getAlcanceGestor(UUID gestorUid) {
        return webClient.get()
                .uri("/api/v1/users/{uid}/alcance", gestorUid)
                .retrieve()
                .bodyToMono(AlcanceGestorResponse.class)
                .onErrorResume(e -> Mono.empty());
    }

    public Mono<AlcanceGestorResponse> fallbackGetAlcanceGestor(UUID gestorUid, Throwable t) {
        return Mono.empty();
    }

    public record AlcanceGestorResponse(
            AlcanceVotacion alcanceOperacion,
            UUID municipioId,
            String municipioNombre,
            UUID departamentoId,
            String departamentoNombre
    ) {}
}
