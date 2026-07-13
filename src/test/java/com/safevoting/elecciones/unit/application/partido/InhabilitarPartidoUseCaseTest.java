package com.safevoting.elecciones.unit.application.partido;

import com.safevoting.elecciones.application.partido.InhabilitarPartidoUseCase;
import com.safevoting.elecciones.domain.exception.partido.PartidoConCandidatosEnVotacionException;
import com.safevoting.elecciones.domain.exception.partido.PartidoNoEncontradoException;
import com.safevoting.elecciones.domain.exception.partido.PartidoYaInhabilitadoException;
import com.safevoting.elecciones.domain.model.candidatura.Candidatura;
import com.safevoting.elecciones.domain.model.partido.EstadoPartido;
import com.safevoting.elecciones.domain.model.partido.PartidoPolitico;
import com.safevoting.elecciones.domain.model.votacion.EstadoVotacion;
import com.safevoting.elecciones.domain.model.votacion.Votacion;
import com.safevoting.elecciones.domain.repository.CandidaturaRepository;
import com.safevoting.elecciones.domain.repository.PartidoPoliticoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InhabilitarPartidoUseCaseTest {

    @Mock
    private PartidoPoliticoRepository partidoRepository;

    @Mock
    private CandidaturaRepository candidaturaRepository;

    @InjectMocks
    private InhabilitarPartidoUseCase useCase;

    @Test
    void inhabilitarExitoso() {
        UUID partidoId = UUID.randomUUID();
        PartidoPolitico partido = PartidoPolitico.builder()
                .id(partidoId).nombre("PARTIDO A").estado(EstadoPartido.HABILITADO).build();

        when(partidoRepository.findById(partidoId)).thenReturn(Mono.just(partido));
        when(candidaturaRepository.findActivasByPartidoId(partidoId)).thenReturn(Flux.empty());
        when(partidoRepository.update(any(PartidoPolitico.class))).thenReturn(Mono.just(partido));

        StepVerifier.create(useCase.ejecutar(partidoId))
                .expectNextMatches(PartidoPolitico::isInhabilitado)
                .verifyComplete();
    }

    @Test
    void partidoYaInhabilitadoShouldThrowPartidoYaInhabilitadoException() {
        UUID partidoId = UUID.randomUUID();
        PartidoPolitico partido = PartidoPolitico.builder()
                .id(partidoId).nombre("PARTIDO A").estado(EstadoPartido.INHABILITADO).build();

        when(partidoRepository.findById(partidoId)).thenReturn(Mono.just(partido));
        when(candidaturaRepository.findActivasByPartidoId(partidoId)).thenReturn(Flux.empty());

        StepVerifier.create(useCase.ejecutar(partidoId))
                .expectError(PartidoYaInhabilitadoException.class)
                .verify();

        verify(partidoRepository, never()).update(any());
    }

    @Test
    void partidoConCandidatosEnVotacionEnProgresoShouldThrowException() {
        UUID partidoId = UUID.randomUUID();
        UUID candidaturaId = UUID.randomUUID();
        PartidoPolitico partido = PartidoPolitico.builder()
                .id(partidoId).nombre("PARTIDO A").estado(EstadoPartido.HABILITADO).build();

        Candidatura candidatura = Candidatura.builder()
                .id(candidaturaId).partidoId(partidoId).build();
        Votacion votacion = Votacion.builder()
                .estado(EstadoVotacion.EN_PROGRESO).build();

        when(partidoRepository.findById(partidoId)).thenReturn(Mono.just(partido));
        when(candidaturaRepository.findActivasByPartidoId(partidoId)).thenReturn(Flux.just(candidatura));
        when(candidaturaRepository.findVotacionByCandidaturaId(candidaturaId)).thenReturn(Mono.just(votacion));

        StepVerifier.create(useCase.ejecutar(partidoId))
                .expectError(PartidoConCandidatosEnVotacionException.class)
                .verify();

        verify(partidoRepository, never()).update(any());
    }

    @Test
    void partidoNoEncontradoShouldThrowPartidoNoEncontradoException() {
        UUID partidoId = UUID.randomUUID();
        when(partidoRepository.findById(partidoId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.ejecutar(partidoId))
                .expectError(PartidoNoEncontradoException.class)
                .verify();
    }
}
