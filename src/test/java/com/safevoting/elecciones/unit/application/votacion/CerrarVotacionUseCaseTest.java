package com.safevoting.elecciones.unit.application.votacion;

import com.safevoting.elecciones.application.votacion.CerrarVotacionUseCase;
import com.safevoting.elecciones.domain.exception.votacion.TransicionEstadoInvalidaException;
import com.safevoting.elecciones.domain.model.votacion.AlcanceVotacion;
import com.safevoting.elecciones.domain.model.votacion.EstadoVotacion;
import com.safevoting.elecciones.domain.model.votacion.TipoVotacion;
import com.safevoting.elecciones.domain.model.votacion.Votacion;
import com.safevoting.elecciones.domain.repository.VotacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CerrarVotacionUseCaseTest {

    @Mock
    private VotacionRepository repository;

    @InjectMocks
    private CerrarVotacionUseCase useCase;

    private final UUID votacionId = UUID.randomUUID();

    @Test
    void cierreExitoso() {
        Votacion votacion = Votacion.builder()
                .id(votacionId).nombre("Test").tipo(TipoVotacion.PRESIDENCIA)
                .alcance(AlcanceVotacion.NACIONAL).estado(EstadoVotacion.EN_PROGRESO).build();

        when(repository.findById(votacionId)).thenReturn(Mono.just(votacion));
        when(repository.update(any(Votacion.class))).thenReturn(Mono.just(votacion));

        StepVerifier.create(useCase.ejecutar(votacionId))
                .expectNextMatches(Votacion::isFinalizada)
                .verifyComplete();
    }

    @Test
    void noEnProgresoLanzaTransicionEstadoInvalidaException() {
        Votacion votacion = Votacion.builder()
                .id(votacionId).nombre("Test").tipo(TipoVotacion.PRESIDENCIA)
                .alcance(AlcanceVotacion.NACIONAL).estado(EstadoVotacion.ACTIVA).build();

        when(repository.findById(votacionId)).thenReturn(Mono.just(votacion));

        StepVerifier.create(useCase.ejecutar(votacionId))
                .expectError(TransicionEstadoInvalidaException.class)
                .verify();

        verify(repository, never()).update(any());
    }
}
