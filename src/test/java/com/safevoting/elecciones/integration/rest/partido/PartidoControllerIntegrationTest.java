package com.safevoting.elecciones.integration.rest.partido;

import com.safevoting.elecciones.EleccionesApplication;
import com.safevoting.elecciones.application.partido.InhabilitarPartidoUseCase;
import com.safevoting.elecciones.domain.model.partido.EstadoPartido;
import com.safevoting.elecciones.domain.model.partido.PartidoPolitico;
import com.safevoting.elecciones.domain.repository.CandidaturaRepository;
import com.safevoting.elecciones.domain.repository.PartidoPoliticoRepository;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.partido.dto.PartidoRequest;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.partido.dto.PartidoResponse;
import com.safevoting.elecciones.infrastructure.config.JwtProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class PartidoControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () ->
                "r2dbc:postgresql://" + postgres.getHost() + ":" + postgres.getMappedPort(5432) + "/test");
        registry.add("spring.r2dbc.username", () -> "test");
        registry.add("spring.r2dbc.password", () -> "test");
        registry.add("spring.flyway.url", () ->
                "jdbc:postgresql://" + postgres.getHost() + ":" + postgres.getMappedPort(5432) + "/test");
        registry.add("spring.flyway.user", () -> "test");
        registry.add("spring.flyway.password", () -> "test");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private PartidoPoliticoRepository repository;

    @MockBean
    private CandidaturaRepository candidaturaRepository;

    private String gestorToken;
    private SecretKey key = Keys.hmacShaKeyFor(
            "test-secret-key-that-is-long-enough-for-hs256-algorithm-minimum-32-characters"
                    .getBytes(StandardCharsets.UTF_8));

    @BeforeEach
    void setUp() {
        gestorToken = Jwts.builder()
                .subject("gestor")
                .claim("roles", List.of("GESTOR_CANDIDATURAS"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }

    @AfterEach
    void cleanUp() {
    }

    @Test
    void crearPartidoShouldReturn201() {
        PartidoRequest request = new PartidoRequest("PARTIDO NUEVO", "Un partido de prueba", null);

        PartidoResponse response = webTestClient.post()
                .uri("/api/v1/partidos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + gestorToken)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PartidoResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.nombre()).isEqualTo("PARTIDO NUEVO");
        assertThat(response.estado()).isEqualTo("HABILITADO");
    }

    @Test
    void crearPartidoNombreDuplicadoShouldReturn409() {
        PartidoRequest request = new PartidoRequest("PARTIDO UNICO", "Desc", null);

        webTestClient.post()
                .uri("/api/v1/partidos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + gestorToken)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/api/v1/partidos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + gestorToken)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void listarPartidosShouldReturn200() {
        cargarPartido("PARTIDO LISTA A");
        cargarPartido("PARTIDO LISTA B");

        webTestClient.get()
                .uri("/api/v1/partidos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + gestorToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PartidoResponse.class)
                .hasSize(2);
    }

    @Test
    void buscarPorIdShouldReturn200() {
        PartidoPolitico partido = cargarPartido("PARTIDO POR ID");

        webTestClient.get()
                .uri("/api/v1/partidos/{id}", partido.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + gestorToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PartidoResponse.class)
                .value(r -> assertThat(r.nombre()).isEqualTo("PARTIDO POR ID"));
    }

    @Test
    void buscarPorIdInexistenteShouldReturn404() {
        webTestClient.get()
                .uri("/api/v1/partidos/{id}", UUID.randomUUID())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + gestorToken)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void editarPartidoShouldReturn200() {
        PartidoPolitico partido = cargarPartido("PARTIDO EDITAR");

        PartidoRequest request = new PartidoRequest("PARTIDO EDITAR", "Descripcion actualizada", null);

        webTestClient.put()
                .uri("/api/v1/partidos/{id}", partido.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + gestorToken)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PartidoResponse.class)
                .value(r -> assertThat(r.descripcion()).isEqualTo("Descripcion actualizada"));
    }

    @Test
    void inhabilitarPartidoShouldReturn200() {
        PartidoPolitico partido = cargarPartido("PARTIDO INHABILITAR");
        when(candidaturaRepository.findActivasByPartidoId(any())).thenReturn(Flux.empty());

        webTestClient.patch()
                .uri("/api/v1/partidos/{id}/inhabilitar", partido.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + gestorToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PartidoResponse.class)
                .value(r -> assertThat(r.estado()).isEqualTo("INHABILITADO"));
    }

    @Test
    void sinTokenShouldReturn401() {
        webTestClient.get()
                .uri("/api/v1/partidos")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    private PartidoPolitico cargarPartido(String nombre) {
        PartidoPolitico partido = PartidoPolitico.builder()
                .nombre(nombre)
                .estado(EstadoPartido.HABILITADO)
                .build();
        return repository.save(partido).block();
    }
}
