package com.safevoting.elecciones.unit.application.partido;

import com.safevoting.elecciones.application.partido.CrearPartidoUseCase;
import com.safevoting.elecciones.domain.exception.partido.NombreDuplicadoException;
import com.safevoting.elecciones.domain.model.partido.EstadoPartido;
import com.safevoting.elecciones.domain.model.partido.PartidoPolitico;
import com.safevoting.elecciones.domain.repository.ImageStorageService;
import com.safevoting.elecciones.domain.repository.PartidoPoliticoRepository;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.partido.dto.PartidoRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrearPartidoUseCaseTest {

    @Mock
    private PartidoPoliticoRepository repository;

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private CrearPartidoUseCase useCase;

    @Test
    void crearPartidoExitoso() {
        PartidoRequest request = new PartidoRequest("PARTIDO A", "Descripcion", null);
        PartidoPolitico partidoGuardado = PartidoPolitico.builder()
                .nombre("PARTIDO A")
                .descripcion("Descripcion")
                .estado(EstadoPartido.HABILITADO)
                .build();

        when(repository.existsByNombre("PARTIDO A")).thenReturn(Mono.just(false));
        when(repository.save(any(PartidoPolitico.class))).thenReturn(Mono.just(partidoGuardado));

        StepVerifier.create(useCase.ejecutar(request))
                .expectNextMatches(p -> "PARTIDO A".equals(p.getNombre())
                        && p.getEstado() == EstadoPartido.HABILITADO)
                .verifyComplete();

        verify(repository).save(any(PartidoPolitico.class));
    }

    @Test
    void nombreDuplicadoShouldThrowNombreDuplicadoException() {
        PartidoRequest request = new PartidoRequest("PARTIDO A", "Descripcion", null);

        when(repository.existsByNombre("PARTIDO A")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.ejecutar(request))
                .expectError(NombreDuplicadoException.class)
                .verify();

        verify(repository, never()).save(any());
    }

    @Test
    void logoUploadFallaShouldCrearPartidoSinLogo() {
        PartidoRequest request = new PartidoRequest("PARTIDO B", "Desc", "base64fake");
        PartidoPolitico partidoGuardado = PartidoPolitico.builder()
                .nombre("PARTIDO B")
                .descripcion("Desc")
                .estado(EstadoPartido.HABILITADO)
                .build();

        when(repository.existsByNombre("PARTIDO B")).thenReturn(Mono.just(false));
        when(imageStorageService.upload(any(byte[].class))).thenReturn(Mono.empty());
        when(repository.save(any(PartidoPolitico.class))).thenReturn(Mono.just(partidoGuardado));

        StepVerifier.create(useCase.ejecutar(request))
                .expectNextMatches(p -> "PARTIDO B".equals(p.getNombre()))
                .verifyComplete();

        verify(repository).save(argThat(p -> p.getLogoUrl() == null || p.getLogoUrl().isEmpty()));
    }
}
