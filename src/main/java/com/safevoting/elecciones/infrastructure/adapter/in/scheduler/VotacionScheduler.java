package com.safevoting.elecciones.infrastructure.adapter.in.scheduler;

import com.safevoting.elecciones.application.votacion.AbrirVotacionUseCase;
import com.safevoting.elecciones.application.votacion.CerrarVotacionUseCase;
import com.safevoting.elecciones.domain.repository.VotacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class VotacionScheduler {

    private static final Logger log = LoggerFactory.getLogger(VotacionScheduler.class);

    private final VotacionRepository votacionRepository;
    private final AbrirVotacionUseCase abrirVotacionUseCase;
    private final CerrarVotacionUseCase cerrarVotacionUseCase;

    public VotacionScheduler(VotacionRepository votacionRepository,
                              AbrirVotacionUseCase abrirVotacionUseCase,
                              CerrarVotacionUseCase cerrarVotacionUseCase) {
        this.votacionRepository = votacionRepository;
        this.abrirVotacionUseCase = abrirVotacionUseCase;
        this.cerrarVotacionUseCase = cerrarVotacionUseCase;
    }

    @Scheduled(fixedRate = 30_000)
    public void abrirVotacionesProgramadas() {
        Instant ahora = Instant.now();
        votacionRepository.findActivasConFechaInicioVencida(ahora)
                .flatMap(v -> {
                    log.info("Abriendo votación programada: {} ({})", v.getNombre(), v.getId());
                    return abrirVotacionUseCase.ejecutar(v.getId());
                })
                .doOnError(e -> log.error("Error al abrir votación programada: {}", e.getMessage(), e))
                .subscribe();
    }

    @Scheduled(fixedRate = 30_000)
    public void cerrarVotacionesProgramadas() {
        Instant ahora = Instant.now();
        votacionRepository.findEnProgresoConFechaFinVencida(ahora)
                .flatMap(v -> {
                    log.info("Cerrando votación programada: {} ({})", v.getNombre(), v.getId());
                    return cerrarVotacionUseCase.ejecutar(v.getId());
                })
                .doOnError(e -> log.error("Error al cerrar votación programada: {}", e.getMessage(), e))
                .subscribe();
    }
}
