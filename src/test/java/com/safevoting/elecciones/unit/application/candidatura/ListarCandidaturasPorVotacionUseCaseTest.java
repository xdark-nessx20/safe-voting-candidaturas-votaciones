package com.safevoting.elecciones.unit.application.candidatura;

import com.safevoting.elecciones.application.candidatura.ListarCandidaturasPorVotacionUseCase;
import com.safevoting.elecciones.application.candidatura.TarjetonItem;
import com.safevoting.elecciones.domain.repository.CandidaturaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarCandidaturasPorVotacionUseCaseTest {

    @Mock
    private CandidaturaRepository candidaturaRepository;

    @InjectMocks
    private ListarCandidaturasPorVotacionUseCase useCase;

    @Test
    void tarjetonConCandidaturas() {
        UUID votacionId = UUID.randomUUID();
        TarjetonItem item = new TarjetonItem(
                UUID.randomUUID(), UUID.randomUUID(), "Juan Perez", "DNI-123",
                "PARTIDO A", "https://logo.png", "https://foto.jpg",
                votacionId, Instant.now(), "ACTIVA"
        );

        when(candidaturaRepository.findTarjetonByVotacionId(votacionId)).thenReturn(Flux.just(item));

        StepVerifier.create(useCase.ejecutar(votacionId))
                .expectNextMatches(i -> "Juan Perez".equals(i.nombreCandidato()))
                .verifyComplete();
    }

    @Test
    void tarjetonVacio() {
        UUID votacionId = UUID.randomUUID();

        when(candidaturaRepository.findTarjetonByVotacionId(votacionId)).thenReturn(Flux.empty());

        StepVerifier.create(useCase.ejecutar(votacionId))
                .verifyComplete();
    }
}
