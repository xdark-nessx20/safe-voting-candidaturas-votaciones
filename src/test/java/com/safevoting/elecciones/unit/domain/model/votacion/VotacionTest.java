package com.safevoting.elecciones.unit.domain.model.votacion;

import com.safevoting.elecciones.domain.exception.votacion.EstadoInvalidoParaModificarFechaException;
import com.safevoting.elecciones.domain.exception.votacion.FechaInicioFuturaException;
import com.safevoting.elecciones.domain.exception.votacion.SinCandidatosException;
import com.safevoting.elecciones.domain.exception.votacion.TransicionEstadoInvalidaException;
import com.safevoting.elecciones.domain.model.votacion.AlcanceVotacion;
import com.safevoting.elecciones.domain.model.votacion.EstadoVotacion;
import com.safevoting.elecciones.domain.model.votacion.TipoVotacion;
import com.safevoting.elecciones.domain.model.votacion.Votacion;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VotacionTest {

    @Test
    void construirConBuilderValoresCorrectos() {
        Votacion v = Votacion.builder()
                .nombre("Elecciones 2026")
                .tipo(TipoVotacion.PRESIDENCIA)
                .alcance(AlcanceVotacion.NACIONAL)
                .build();

        assertEquals("Elecciones 2026", v.getNombre());
        assertEquals(TipoVotacion.PRESIDENCIA, v.getTipo());
        assertEquals(AlcanceVotacion.NACIONAL, v.getAlcance());
        assertEquals(EstadoVotacion.ACTIVA, v.getEstado());
    }

    @Test
    void abrirConCandidatosYFechaOk() {
        Votacion v = buildActiva();
        assertDoesNotThrow(() -> v.abrir(true));
        assertTrue(v.isEnProgreso());
    }

    @Test
    void abrirSinCandidatosLanzaSinCandidatosException() {
        Votacion v = buildActiva();
        assertThrows(SinCandidatosException.class, () -> v.abrir(false));
    }

    @Test
    void abrirAntesDeFechaInicioLanzaFechaInicioFuturaException() {
        Votacion v = buildActiva();
        v.establecerFechas(Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200));
        assertThrows(FechaInicioFuturaException.class, () -> v.abrir(true));
    }

    @Test
    void cerrarEnProgresoTransicionCorrecta() {
        Votacion v = buildEnProgreso();
        assertDoesNotThrow(v::cerrar);
        assertTrue(v.isFinalizada());
    }

    @Test
    void cerrarActivaLanzaTransicionEstadoInvalidaException() {
        Votacion v = buildActiva();
        assertThrows(TransicionEstadoInvalidaException.class, v::cerrar);
    }

    @Test
    void cancelarActivaConMotivoTransicionCorrecta() {
        Votacion v = buildActiva();
        assertDoesNotThrow(() -> v.cancelar("Motivo de cancelación suficiente"));
        assertTrue(v.isCancelada());
        assertEquals("Motivo de cancelación suficiente", v.getMotivo());
    }

    @Test
    void cancelarSinMotivoLanzaMotivoRequeridoException() {
        Votacion v = buildActiva();
        assertThrows(com.safevoting.elecciones.domain.exception.votacion.MotivoRequeridoException.class,
                () -> v.cancelar(""));
    }

    @Test
    void establecerFechasEnActivaOk() {
        Votacion v = buildActiva();
        Instant inicio = Instant.parse("2026-01-01T00:00:00Z");
        Instant fin = Instant.parse("2026-01-02T00:00:00Z");
        assertDoesNotThrow(() -> v.establecerFechas(inicio, fin));
        assertEquals(inicio, v.getFechaInicio());
        assertEquals(fin, v.getFechaFin());
    }

    @Test
    void establecerFechasEnProgresoLanzaEstadoInvalidoParaModificarFechaException() {
        Votacion v = buildEnProgreso();
        assertThrows(EstadoInvalidoParaModificarFechaException.class,
                () -> v.establecerFechas(Instant.now(), Instant.now()));
    }

    @Test
    void completarDesdeFinalizadaTransicionCorrecta() {
        Votacion v = buildFinalizada();
        assertDoesNotThrow(v::completar);
        assertTrue(v.isCompletada());
    }

    @Test
    void completarDesdeActivaLanzaTransicionEstadoInvalidaException() {
        Votacion v = buildActiva();
        assertThrows(TransicionEstadoInvalidaException.class, v::completar);
    }

    @Test
    void alcanceCubreJerarquia() {
        assertTrue(AlcanceVotacion.NACIONAL.cubre(AlcanceVotacion.MUNICIPAL));
        assertTrue(AlcanceVotacion.NACIONAL.cubre(AlcanceVotacion.DEPARTAMENTAL));
        assertTrue(AlcanceVotacion.NACIONAL.cubre(AlcanceVotacion.REGIONAL));
        assertTrue(AlcanceVotacion.DEPARTAMENTAL.cubre(AlcanceVotacion.MUNICIPAL));
        assertFalse(AlcanceVotacion.MUNICIPAL.cubre(AlcanceVotacion.NACIONAL));
    }

    @Test
    void tipoAlcanceCompatible() {
        assertEquals(AlcanceVotacion.NACIONAL, TipoVotacion.PRESIDENCIA.alcanceCompatible());
        assertEquals(AlcanceVotacion.NACIONAL, TipoVotacion.CONGRESO.alcanceCompatible());
        assertEquals(AlcanceVotacion.MUNICIPAL, TipoVotacion.ALCALDIA.alcanceCompatible());
        assertEquals(AlcanceVotacion.DEPARTAMENTAL, TipoVotacion.GOBERNACION.alcanceCompatible());
    }

    @Test
    void validateInfoAlcanceMunicipalRequiereMunicipioId() {
        Votacion v = Votacion.builder()
                .nombre("Alcaldia X")
                .tipo(TipoVotacion.ALCALDIA)
                .alcance(AlcanceVotacion.MUNICIPAL)
                .municipioId(null)
                .build();

        assertThrows(com.safevoting.elecciones.domain.exception.DatosInvalidosException.class, v::validateInfo);
    }

    @Test
    void reactivarDesdeCanceladaOk() {
        Votacion v = buildCancelada();
        assertDoesNotThrow(v::reactivar);
        assertTrue(v.isActiva());
        assertNull(v.getMotivo());
    }

    @Test
    void reactivarDesdeActivaLanzaTransicionEstadoInvalidaException() {
        Votacion v = buildActiva();
        assertThrows(TransicionEstadoInvalidaException.class, v::reactivar);
    }

    private Votacion buildActiva() {
        return Votacion.builder()
                .id(UUID.randomUUID())
                .nombre("Test")
                .tipo(TipoVotacion.PRESIDENCIA)
                .alcance(AlcanceVotacion.NACIONAL)
                .estado(EstadoVotacion.ACTIVA)
                .build();
    }

    private Votacion buildEnProgreso() {
        return Votacion.builder()
                .id(UUID.randomUUID())
                .nombre("Test")
                .tipo(TipoVotacion.PRESIDENCIA)
                .alcance(AlcanceVotacion.NACIONAL)
                .estado(EstadoVotacion.EN_PROGRESO)
                .build();
    }

    private Votacion buildFinalizada() {
        return Votacion.builder()
                .id(UUID.randomUUID())
                .nombre("Test")
                .tipo(TipoVotacion.PRESIDENCIA)
                .alcance(AlcanceVotacion.NACIONAL)
                .estado(EstadoVotacion.FINALIZADA)
                .build();
    }

    private Votacion buildCancelada() {
        return Votacion.builder()
                .id(UUID.randomUUID())
                .nombre("Test")
                .tipo(TipoVotacion.PRESIDENCIA)
                .alcance(AlcanceVotacion.NACIONAL)
                .estado(EstadoVotacion.CANCELADA)
                .motivo("Motivo previo")
                .build();
    }
}
