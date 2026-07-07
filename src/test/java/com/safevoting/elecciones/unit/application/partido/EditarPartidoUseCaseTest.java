package com.safevoting.elecciones.unit.application.partido;

import com.safevoting.elecciones.application.partido.EditarPartidoUseCase;
import com.safevoting.elecciones.domain.exception.partido.NombreDuplicadoException;
import com.safevoting.elecciones.domain.exception.partido.PartidoNoEncontradoException;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EditarPartidoUseCaseTest {

    @Mock
    private PartidoPoliticoRepository repository;

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private EditarPartidoUseCase useCase;

    @Test
    void editarDescripcionShouldSucceed() {
        UUID id = UUID.randomUUID();
        PartidoPolitico existente = PartidoPolitico.builder()
                .id(id).nombre("PARTIDO A").descripcion("Vieja desc").logoUrl("https://old-logo.png")
                .estado(EstadoPartido.HABILITADO).build();
        PartidoRequest request = new PartidoRequest("PARTIDO A", "Nueva desc", null);

        when(repository.findById(id)).thenReturn(Mono.just(existente));
        when(repository.update(any(PartidoPolitico.class))).thenReturn(Mono.just(existente));

        StepVerifier.create(useCase.ejecutar(id, request))
                .expectNextMatches(p -> "Nueva desc".equals(p.getDescripcion()))
                .verifyComplete();
    }

    @Test
    void cambiarNombreADuplicadoShouldThrowNombreDuplicadoException() {
        UUID id = UUID.randomUUID();
        PartidoPolitico existente = PartidoPolitico.builder()
                .id(id).nombre("PARTIDO A").estado(EstadoPartido.HABILITADO).build();
        PartidoRequest request = new PartidoRequest("PARTIDO B", "Desc", null);

        when(repository.findById(id)).thenReturn(Mono.just(existente));
        when(repository.existsByNombre("PARTIDO B")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.ejecutar(id, request))
                .expectError(NombreDuplicadoException.class)
                .verify();

        verify(repository, never()).update(any());
    }

    @Test
    void partidoNoEncontradoShouldThrowPartidoNoEncontradoException() {
        UUID id = UUID.randomUUID();
        PartidoRequest request = new PartidoRequest("PARTIDO X", "Desc", null);

        when(repository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.ejecutar(id, request))
                .expectError(PartidoNoEncontradoException.class)
                .verify();
    }
}
