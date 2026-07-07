package com.safevoting.elecciones.domain.repository;

import reactor.core.publisher.Mono;

public interface ImageStorageService {

    Mono<String> upload(byte[] image);
}
