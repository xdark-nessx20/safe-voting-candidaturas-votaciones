package com.safevoting.elecciones.application.miembro;

import com.safevoting.elecciones.domain.exception.miembro.MiembroDuplicadoException;
import com.safevoting.elecciones.domain.exception.partido.PartidoNoEncontradoException;
import com.safevoting.elecciones.domain.exception.partido.PartidoYaInhabilitadoException;
import com.safevoting.elecciones.domain.model.partido.MiembroPartido;
import com.safevoting.elecciones.domain.model.partido.PartidoPolitico;
import com.safevoting.elecciones.domain.repository.ImageStorageService;
import com.safevoting.elecciones.domain.repository.MiembroPartidoRepository;
import com.safevoting.elecciones.domain.repository.PartidoPoliticoRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.UUID;

@RequiredArgsConstructor
public class CrearMiembroUseCase {

    private final MiembroPartidoRepository repository;
    private final PartidoPoliticoRepository partidoRepository;
    private final ImageStorageService imageStorageService;

    public Mono<MiembroPartido> ejecutar(UUID partidoId, MiembroPartido miembro, String fotoBase64) {
        return validarPartidoHabilitado(partidoId)
                .then(Mono.defer(() -> validarNoDuplicado(miembro.getUsuarioId(), partidoId)))
                .then(Mono.defer(() -> validarYGuardar(miembro, fotoBase64)));
    }

    private Mono<Void> validarPartidoHabilitado(UUID partidoId) {
        return partidoRepository.findById(partidoId)
                .switchIfEmpty(Mono.error(new PartidoNoEncontradoException(partidoId)))
                .filter(PartidoPolitico::isHabilitado)
                .switchIfEmpty(Mono.error(new PartidoYaInhabilitadoException(partidoId)))
                .then();
    }

    private Mono<Void> validarNoDuplicado(UUID usuarioId, UUID partidoId) {
        return repository.existsByUsuarioIdAndPartidoId(usuarioId, partidoId)
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new MiembroDuplicadoException(usuarioId, partidoId)))
                .then();
    }

    private Mono<MiembroPartido> validarYGuardar(MiembroPartido miembro, String fotoBase64) {
        miembro.validateInfo();
        return subirFotoYGuardar(miembro, fotoBase64);
    }

    private Mono<MiembroPartido> subirFotoYGuardar(MiembroPartido miembro, String fotoBase64) {
        return Mono.justOrEmpty(fotoBase64)
                .filter(f -> !f.isBlank())
                .flatMap(f -> decodificarYSubir(f).onErrorResume(e -> Mono.empty()))
                .flatMap(fotoUrl -> {
                    miembro.changeFoto(fotoUrl);
                    return repository.save(miembro);
                })
                .switchIfEmpty(repository.save(miembro));
    }

    private Mono<String> decodificarYSubir(String fotoBase64) {
        try {
            return imageStorageService.upload(Base64.getDecoder().decode(fotoBase64));
        } catch (IllegalArgumentException e) {
            return Mono.empty();
        }
    }
}
