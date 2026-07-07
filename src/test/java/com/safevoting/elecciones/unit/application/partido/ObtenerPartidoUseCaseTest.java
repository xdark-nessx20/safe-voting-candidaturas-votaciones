package com.safevoting.elecciones.unit.application.partido;

import com.safevoting.elecciones.application.partido.ObtenerPartidoUseCase;
import com.safevoting.elecciones.domain.exception.partido.PartidoNoEncontradoException;
import com.safevoting.elecciones.domain.model.partido.EstadoPartido;
import com.safevoting.elecciones.domain.model.partido.PartidoPolitico;
import com.safevoting.elecciones.domain.repository.PartidoPoliticoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObtenerPartidoUseCaseTest {

    @Mock
    private PartidoPoliticoRepository repository;

    @InjectMocks
    private ObtenerPartidoUseCase useCase;

    @Test
    void buscarPorIdExistenteShouldReturnPartido() {
        UUID id = UUID.randomUUID();
        PartidoPolitico partido = PartidoPolitico.builder()
                .id(id).nombre("PARTIDO A").estado(EstadoPartido.HABILITADO).build();

        when(repository.findById(id)).thenReturn(Mono.just(partido));

        StepVerifier.create(useCase.ejecutar(id))
                .expectNextMatches(p -> p.getNombre().equals("PARTIDO A"))
                .verifyComplete();
    }

    @Test
    void buscarPorIdInexistenteShouldThrowPartidoNoEncontradoException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.ejecutar(id))
                .expectError(PartidoNoEncontradoException.class)
                .verify();
    }
}
