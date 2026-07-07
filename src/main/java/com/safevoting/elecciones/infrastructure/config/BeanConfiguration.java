package com.safevoting.elecciones.infrastructure.config;

import com.safevoting.elecciones.application.partido.CrearPartidoUseCase;
import com.safevoting.elecciones.application.partido.EditarPartidoUseCase;
import com.safevoting.elecciones.application.partido.InhabilitarPartidoUseCase;
import com.safevoting.elecciones.application.partido.ListarPartidosUseCase;
import com.safevoting.elecciones.application.partido.ObtenerPartidoUseCase;
import com.safevoting.elecciones.domain.repository.CandidaturaRepository;
import com.safevoting.elecciones.domain.repository.ImageStorageService;
import com.safevoting.elecciones.domain.repository.PartidoPoliticoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public CrearPartidoUseCase crearPartidoUseCase(PartidoPoliticoRepository repository, ImageStorageService imageStorageService) {
        return new CrearPartidoUseCase(repository, imageStorageService);
    }

    @Bean
    public EditarPartidoUseCase editarPartidoUseCase(PartidoPoliticoRepository repository, ImageStorageService imageStorageService) {
        return new EditarPartidoUseCase(repository, imageStorageService);
    }

    @Bean
    public InhabilitarPartidoUseCase inhabilitarPartidoUseCase(PartidoPoliticoRepository partidoRepository, CandidaturaRepository candidaturaRepository) {
        return new InhabilitarPartidoUseCase(partidoRepository, candidaturaRepository);
    }

    @Bean
    public ListarPartidosUseCase listarPartidosUseCase(PartidoPoliticoRepository repository) {
        return new ListarPartidosUseCase(repository);
    }

    @Bean
    public ObtenerPartidoUseCase obtenerPartidoUseCase(PartidoPoliticoRepository repository) {
        return new ObtenerPartidoUseCase(repository);
    }
}
