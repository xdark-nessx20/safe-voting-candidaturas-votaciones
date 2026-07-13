package com.safevoting.elecciones.unit.application.votacion;

import com.safevoting.elecciones.application.votacion.CrearVotacionUseCase;
import com.safevoting.elecciones.domain.exception.votacion.NombreDuplicadoException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrearVotacionUseCaseTest {

    @Mock
    private VotacionRepository repository;

    @InjectMocks
    private CrearVotacionUseCase useCase;

    @Test
    void creacionExitosa() {
        Votacion votacion = Votacion.builder()
                .nombre("Elecciones 2026")
                .tipo(TipoVotacion.PRESIDENCIA)
                .alcance(AlcanceVotacion.NACIONAL)
                .build();

        when(repository.existsByNombre("Elecciones 2026")).thenReturn(Mono.just(false));
        when(repository.save(any(Votacion.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.ejecutar(votacion))
                .expectNextMatches(v -> v.isActiva() && "Elecciones 2026".equals(v.getNombre()))
                .verifyComplete();
    }

    @Test
    void nombreDuplicadoLanzaNombreDuplicadoException() {
        Votacion votacion = Votacion.builder()
                .nombre("Duplicada")
                .tipo(TipoVotacion.PRESIDENCIA)
                .alcance(AlcanceVotacion.NACIONAL)
                .build();

        when(repository.existsByNombre("Duplicada")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.ejecutar(votacion))
                .expectError(NombreDuplicadoException.class)
                .verify();

        verify(repository, never()).save(any());
    }
}
