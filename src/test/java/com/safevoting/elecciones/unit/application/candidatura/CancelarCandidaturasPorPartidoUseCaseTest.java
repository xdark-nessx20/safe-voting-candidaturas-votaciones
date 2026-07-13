package com.safevoting.elecciones.unit.application.candidatura;

import com.safevoting.elecciones.application.candidatura.CancelarCandidaturasPorPartidoUseCase;
import com.safevoting.elecciones.domain.repository.CandidaturaRepository;
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
class CancelarCandidaturasPorPartidoUseCaseTest {

    @Mock
    private CandidaturaRepository candidaturaRepository;

    @InjectMocks
    private CancelarCandidaturasPorPartidoUseCase useCase;

    @Test
    void cancelacionEnCascadaExitosa() {
        UUID partidoId = UUID.randomUUID();
        when(candidaturaRepository.cancelarByPartidoId(partidoId)).thenReturn(Mono.just(3L));

        StepVerifier.create(useCase.ejecutar(partidoId))
                .expectNext(3L)
                .verifyComplete();
    }
}
