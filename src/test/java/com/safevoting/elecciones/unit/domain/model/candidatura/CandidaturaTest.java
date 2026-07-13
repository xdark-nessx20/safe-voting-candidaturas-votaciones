package com.safevoting.elecciones.unit.domain.model.candidatura;

import com.safevoting.elecciones.domain.exception.candidatura.CandidaturaYaCanceladaException;
import com.safevoting.elecciones.domain.model.candidatura.Candidatura;
import com.safevoting.elecciones.domain.model.candidatura.EstadoCandidatura;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CandidaturaTest {

    @Test
    void construirConBuilderYValidar() {
        Candidatura c = Candidatura.builder()
                .miembroPartidoId(UUID.randomUUID())
                .partidoId(UUID.randomUUID())
                .votacionId(UUID.randomUUID())
                .build();

        assertDoesNotThrow(c::validateInfo);
        assertEquals(EstadoCandidatura.ACTIVA, c.getEstado());
        assertTrue(c.isActiva());
    }

    @Test
    void cancelarActivaTransicionCorrecta() {
        Candidatura c = Candidatura.builder()
                .miembroPartidoId(UUID.randomUUID())
                .partidoId(UUID.randomUUID())
                .votacionId(UUID.randomUUID())
                .build();

        assertDoesNotThrow(c::cancelar);
        assertFalse(c.isActiva());
    }

    @Test
    void cancelarYaCanceladaLanzaCandidaturaYaCanceladaException() {
        Candidatura c = Candidatura.builder()
                .miembroPartidoId(UUID.randomUUID())
                .partidoId(UUID.randomUUID())
                .votacionId(UUID.randomUUID())
                .estado(EstadoCandidatura.CANCELADA)
                .build();

        assertThrows(CandidaturaYaCanceladaException.class, c::cancelar);
    }
}
