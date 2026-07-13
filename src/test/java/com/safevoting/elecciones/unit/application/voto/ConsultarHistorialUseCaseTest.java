package com.safevoting.elecciones.unit.application.voto;

import com.safevoting.elecciones.application.voto.ConsultarHistorialUseCase;
import com.safevoting.elecciones.domain.model.participacion.EstadoParticipacion;
import com.safevoting.elecciones.domain.model.participacion.Participacion;
import com.safevoting.elecciones.domain.model.votacion.Votacion;
import com.safevoting.elecciones.domain.repository.ParticipacionRepository;
import com.safevoting.elecciones.domain.repository.VotacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarHistorialUseCaseTest {

    @Mock
    private ParticipacionRepository participacionRepository;

    @Mock
    private VotacionRepository votacionRepository;

    @InjectMocks
    private ConsultarHistorialUseCase useCase;

    @Test
    void historialConParticipaciones() {
        UUID usuarioId = UUID.randomUUID();
        UUID votacionId = UUID.randomUUID();
        UUID participacionId = UUID.randomUUID();
        Instant fecha = Instant.now();

        Participacion participacion = Participacion.builder()
                .id(participacionId).usuarioId(usuarioId).votoId(UUID.randomUUID())
                .votacionId(votacionId).estado(EstadoParticipacion.VALIDO).fechaEmision(fecha).build();
        Votacion votacion = Votacion.builder().id(votacionId).nombre("Elecciones 2026").build();

        when(participacionRepository.findByUsuarioId(usuarioId)).thenReturn(Flux.just(participacion));
        when(votacionRepository.findById(votacionId)).thenReturn(Mono.just(votacion));

        StepVerifier.create(useCase.ejecutar(usuarioId))
                .expectNextMatches(h -> "Elecciones 2026".equals(h.votacionNombre())
                        && "VALIDO".equals(h.estado()))
                .verifyComplete();
    }

    @Test
    void historialVacio() {
        UUID usuarioId = UUID.randomUUID();

        when(participacionRepository.findByUsuarioId(usuarioId)).thenReturn(Flux.empty());

        StepVerifier.create(useCase.ejecutar(usuarioId))
                .verifyComplete();
    }
}
