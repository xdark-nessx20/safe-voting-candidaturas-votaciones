package com.safevoting.elecciones.unit.application.partido;

import com.safevoting.elecciones.application.partido.ListarPartidosUseCase;
import com.safevoting.elecciones.domain.model.partido.EstadoPartido;
import com.safevoting.elecciones.domain.model.partido.PartidoPolitico;
import com.safevoting.elecciones.domain.repository.PartidoPoliticoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarPartidosUseCaseTest {

    @Mock
    private PartidoPoliticoRepository repository;

    @InjectMocks
    private ListarPartidosUseCase useCase;

    @Test
    void listarConVariosPartidosShouldReturnAll() {
        PartidoPolitico p1 = PartidoPolitico.builder()
                .id(UUID.randomUUID()).nombre("PARTIDO A").estado(EstadoPartido.HABILITADO).build();
        PartidoPolitico p2 = PartidoPolitico.builder()
                .id(UUID.randomUUID()).nombre("PARTIDO B").estado(EstadoPartido.HABILITADO).build();

        when(repository.findAll()).thenReturn(Flux.just(p1, p2));

        StepVerifier.create(useCase.ejecutar())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void listarVacioShouldReturnEmpty() {
        when(repository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(useCase.ejecutar())
                .verifyComplete();
    }
}
