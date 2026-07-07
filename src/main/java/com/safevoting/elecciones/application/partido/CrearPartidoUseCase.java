package com.safevoting.elecciones.application.partido;

import com.safevoting.elecciones.domain.exception.partido.NombreDuplicadoException;
import com.safevoting.elecciones.domain.model.partido.PartidoPolitico;
import com.safevoting.elecciones.domain.repository.ImageStorageService;
import com.safevoting.elecciones.domain.repository.PartidoPoliticoRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Base64;

@RequiredArgsConstructor
public class CrearPartidoUseCase {

    private final PartidoPoliticoRepository repository;
    private final ImageStorageService imageStorageService;

    public Mono<PartidoPolitico> ejecutar(PartidoPolitico partido, String logoBase64) {
        return repository.existsByNombre(partido.getNombre())
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new NombreDuplicadoException(partido.getNombre())))
                .then(uploadLogoIfPresent(logoBase64)
                        .defaultIfEmpty("")
                        .map(logoUrl -> {
                            PartidoPolitico newPartido = buildPartido(partido, logoUrl);
                            newPartido.validateInfo();
                            return newPartido;
                        })
                        .flatMap(repository::save));
    }

    private PartidoPolitico buildPartido(PartidoPolitico partido, String logoUrl){
        return PartidoPolitico.builder()
                .nombre(partido.getNombre())
                .descripcion(partido.getDescripcion())
                .logoUrl(logoUrl.isBlank() ? null : logoUrl)
                .build();
    }

    private Mono<String> uploadLogoIfPresent(String logoBase64) {
        if (logoBase64 == null || logoBase64.isBlank()) {
            return Mono.empty();
        }
        byte[] imageBytes = Base64.getDecoder().decode(logoBase64);
        return imageStorageService.upload(imageBytes);
    }
}
