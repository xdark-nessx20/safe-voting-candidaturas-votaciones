package com.safevoting.elecciones.infrastructure.adapter.out.http;

import com.safevoting.elecciones.infrastructure.adapter.out.http.dto.UsuarioResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class UsuarioServiceClient {

    private final WebClient webClient;

    public UsuarioServiceClient(WebClient modulo1WebClient) {
        this.webClient = modulo1WebClient;
    }

    @CircuitBreaker(name = "modulo1", fallbackMethod = "fallbackGetUsuario")
    public Mono<UsuarioResponse> getUsuario(UUID usuarioId) {
        return webClient.get()
                .uri("/api/v1/users/{usuarioId}", usuarioId)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() && status.value() != 404,
                        response -> Mono.error(new RuntimeException("Error del cliente Módulo 1")))
                .bodyToMono(UsuarioResponse.class)
                .onErrorResume(e -> Mono.empty());
    }

    public Mono<UsuarioResponse> fallbackGetUsuario(UUID usuarioId, Throwable t) {
        return Mono.empty();
    }
}
