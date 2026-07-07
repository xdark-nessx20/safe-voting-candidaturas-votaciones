package com.safevoting.elecciones.application.partido;

import com.safevoting.elecciones.domain.exception.partido.NombreDuplicadoException;
import com.safevoting.elecciones.domain.exception.partido.PartidoNoEncontradoException;
import com.safevoting.elecciones.domain.model.partido.PartidoPolitico;
import com.safevoting.elecciones.domain.repository.ImageStorageService;
import com.safevoting.elecciones.domain.repository.PartidoPoliticoRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.UUID;

@RequiredArgsConstructor
public class EditarPartidoUseCase {

    private final PartidoPoliticoRepository repository;
    private final ImageStorageService imageStorageService;

    public Mono<PartidoPolitico> ejecutar(UUID id, PartidoPolitico newPartido, String logoBase64) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new PartidoNoEncontradoException(id)))
                .flatMap(partido -> {
                    boolean nombreCambio = !partido.getNombre().equals(newPartido.getNombre());
                    return validarNombreSiCambio(nombreCambio, newPartido.getNombre())
                            .then(uploadOrKeepLogo(logoBase64, partido.getLogoUrl()).defaultIfEmpty(""))
                            .flatMap(nuevoLogoUrl -> {
                                partido.setNombre(newPartido.getNombre());
                                partido.setDescripcion(newPartido.getDescripcion());
                                partido.changeLogo(nuevoLogoUrl);
                                return repository.update(partido);
                            });
                });
    }

    private Mono<Void> validarNombreSiCambio(boolean nombreCambio, String nombre) {
        if (!nombreCambio) {
            return Mono.empty();
        }
        return repository.existsByNombre(nombre)
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new NombreDuplicadoException(nombre)))
                .then();
    }

    private Mono<String> uploadOrKeepLogo(String logoBase64, String logoActual) {
        if (logoBase64 == null || logoBase64.isBlank()) {
            return Mono.justOrEmpty(logoActual);
        }
        byte[] imageBytes = Base64.getDecoder().decode(logoBase64);
        return imageStorageService.upload(imageBytes)
                .switchIfEmpty(Mono.justOrEmpty(logoActual));
    }
}
