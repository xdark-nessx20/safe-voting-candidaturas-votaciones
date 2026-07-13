package com.safevoting.elecciones.unit.application.miembro;

import com.safevoting.elecciones.application.miembro.CrearMiembroUseCase;
import com.safevoting.elecciones.domain.exception.miembro.MiembroDuplicadoException;
import com.safevoting.elecciones.domain.exception.partido.PartidoNoEncontradoException;
import com.safevoting.elecciones.domain.exception.partido.PartidoYaInhabilitadoException;
import com.safevoting.elecciones.domain.model.partido.EstadoPartido;
import com.safevoting.elecciones.domain.model.partido.MiembroPartido;
import com.safevoting.elecciones.domain.model.partido.PartidoPolitico;
import com.safevoting.elecciones.domain.repository.ImageStorageService;
import com.safevoting.elecciones.domain.repository.MiembroPartidoRepository;
import com.safevoting.elecciones.domain.repository.PartidoPoliticoRepository;
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
class CrearMiembroUseCaseTest {

    @Mock
    private MiembroPartidoRepository miembroRepository;

    @Mock
    private PartidoPoliticoRepository partidoRepository;

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private CrearMiembroUseCase useCase;

    private final UUID partidoId = UUID.randomUUID();
    private final UUID usuarioId = UUID.randomUUID();

    @Test
    void miembroVerificadoSeCreaCorrectamente() {
        MiembroPartido miembro = MiembroPartido.builder()
                .usuarioId(usuarioId)
                .partidoId(partidoId)
                .nombreCompleto("Juan Perez")
                .documentoIdentidad("DNI-12345678")
                .lugarInscripcion("Lima")
                .verificado(true)
                .build();
        PartidoPolitico partido = PartidoPolitico.builder()
                .id(partidoId).nombre("PARTIDO A").estado(EstadoPartido.HABILITADO).build();

        when(partidoRepository.findById(partidoId)).thenReturn(Mono.just(partido));
        when(miembroRepository.existsByUsuarioIdAndPartidoId(usuarioId, partidoId)).thenReturn(Mono.just(false));
        when(miembroRepository.save(any(MiembroPartido.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.ejecutar(partidoId, miembro, null))
                .expectNextMatches(m -> m.isVerificado()
                        && "Juan Perez".equals(m.getNombreCompleto())
                        && "DNI-12345678".equals(m.getDocumentoIdentidad())
                        && "Lima".equals(m.getLugarInscripcion()))
                .verifyComplete();
    }

    @Test
    void miembroNoVerificadoSeCreaCorrectamente() {
        MiembroPartido miembro = MiembroPartido.builder()
                .usuarioId(usuarioId)
                .partidoId(partidoId)
                .nombreCompleto("Juan Perez")
                .documentoIdentidad("DNI-12345678")
                .lugarInscripcion("Lima")
                .verificado(false)
                .build();
        PartidoPolitico partido = PartidoPolitico.builder()
                .id(partidoId).nombre("PARTIDO A").estado(EstadoPartido.HABILITADO).build();

        when(partidoRepository.findById(partidoId)).thenReturn(Mono.just(partido));
        when(miembroRepository.existsByUsuarioIdAndPartidoId(usuarioId, partidoId)).thenReturn(Mono.just(false));
        when(miembroRepository.save(any(MiembroPartido.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.ejecutar(partidoId, miembro, null))
                .expectNextMatches(m -> !m.isVerificado()
                        && "Juan Perez".equals(m.getNombreCompleto()))
                .verifyComplete();
    }

    @Test
    void partidoInhabilitadoLanzaPartidoYaInhabilitadoException() {
        MiembroPartido miembro = MiembroPartido.builder()
                .usuarioId(usuarioId).partidoId(partidoId).nombreCompleto("Juan").documentoIdentidad("DNI-123").lugarInscripcion("Lima")
                .build();
        PartidoPolitico partido = PartidoPolitico.builder()
                .id(partidoId).nombre("PARTIDO A").estado(EstadoPartido.INHABILITADO).build();

        when(partidoRepository.findById(partidoId)).thenReturn(Mono.just(partido));

        StepVerifier.create(useCase.ejecutar(partidoId, miembro, null))
                .expectError(PartidoYaInhabilitadoException.class)
                .verify();

        verify(miembroRepository, never()).save(any());
    }

    @Test
    void usuarioYaMiembroLanzaMiembroDuplicadoException() {
        MiembroPartido miembro = MiembroPartido.builder()
                .usuarioId(usuarioId).partidoId(partidoId).nombreCompleto("Juan").documentoIdentidad("DNI-123").lugarInscripcion("Lima")
                .build();
        PartidoPolitico partido = PartidoPolitico.builder()
                .id(partidoId).nombre("PARTIDO A").estado(EstadoPartido.HABILITADO).build();

        when(partidoRepository.findById(partidoId)).thenReturn(Mono.just(partido));
        when(miembroRepository.existsByUsuarioIdAndPartidoId(usuarioId, partidoId)).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.ejecutar(partidoId, miembro, null))
                .expectError(MiembroDuplicadoException.class)
                .verify();

        verify(miembroRepository, never()).save(any());
    }

    @Test
    void partidoNoEncontradoLanzaPartidoNoEncontradoException() {
        MiembroPartido miembro = MiembroPartido.builder()
                .usuarioId(usuarioId).partidoId(partidoId).nombreCompleto("Juan").documentoIdentidad("DNI-123").lugarInscripcion("Lima")
                .build();

        when(partidoRepository.findById(partidoId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.ejecutar(partidoId, miembro, null))
                .expectError(PartidoNoEncontradoException.class)
                .verify();

        verify(miembroRepository, never()).save(any());
    }

    @Test
    void fotoUploadFallaContinuaSinFoto() {
        MiembroPartido miembro = MiembroPartido.builder()
                .usuarioId(usuarioId)
                .partidoId(partidoId)
                .nombreCompleto("Juan Perez")
                .documentoIdentidad("DNI-12345678")
                .lugarInscripcion("Lima")
                .verificado(true)
                .build();
        PartidoPolitico partido = PartidoPolitico.builder()
                .id(partidoId).nombre("PARTIDO A").estado(EstadoPartido.HABILITADO).build();

        when(partidoRepository.findById(partidoId)).thenReturn(Mono.just(partido));
        when(miembroRepository.existsByUsuarioIdAndPartidoId(usuarioId, partidoId)).thenReturn(Mono.just(false));
        when(imageStorageService.upload(any(byte[].class))).thenReturn(Mono.empty());
        when(miembroRepository.save(any(MiembroPartido.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.ejecutar(partidoId, miembro, "fakeBase64"))
                .expectNextMatches(m -> m.getFotoUrl() == null && m.isVerificado())
                .verifyComplete();
    }
}
