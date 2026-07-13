package com.safevoting.elecciones.application.miembro;

import com.safevoting.elecciones.domain.exception.miembro.MiembroNoEncontradoException;
import com.safevoting.elecciones.domain.model.partido.MiembroPartido;
import com.safevoting.elecciones.domain.repository.ImageStorageService;
import com.safevoting.elecciones.domain.repository.MiembroPartidoRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.UUID;

@RequiredArgsConstructor
public class EditarMiembroUseCase {

    private final MiembroPartidoRepository repository;
    private final ImageStorageService imageStorageService;

    public Mono<MiembroPartido> ejecutar(UUID id, String fotoBase64) {
        return buscarMiembro(id)
                .flatMap(miembro -> actualizarFotoYGuardar(miembro, fotoBase64));
    }

    private Mono<MiembroPartido> buscarMiembro(UUID id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new MiembroNoEncontradoException(id)));
    }

    private Mono<MiembroPartido> actualizarFotoYGuardar(MiembroPartido miembro, String fotoBase64) {
        return Mono.justOrEmpty(fotoBase64)
                .filter(f -> !f.isBlank())
                .flatMap(f -> decodificarYSubir(f).onErrorResume(e -> Mono.just(miembro.getFotoUrl())))
                .flatMap(fotoUrl -> {
                    miembro.changeFoto(fotoUrl);
                    return repository.update(miembro);
                })
                .switchIfEmpty(repository.update(miembro));
    }

    private Mono<String> decodificarYSubir(String fotoBase64) {
        try {
            return imageStorageService.upload(Base64.getDecoder().decode(fotoBase64));
        } catch (IllegalArgumentException e) {
            return Mono.empty();
        }
    }
}
