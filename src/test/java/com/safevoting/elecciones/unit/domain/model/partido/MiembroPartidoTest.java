package com.safevoting.elecciones.unit.domain.model.partido;

import com.safevoting.elecciones.domain.exception.DatosInvalidosException;
import com.safevoting.elecciones.domain.exception.miembro.MiembroYaInactivoException;
import com.safevoting.elecciones.domain.model.partido.EstadoMiembro;
import com.safevoting.elecciones.domain.model.partido.MiembroPartido;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MiembroPartidoTest {

    @Test
    void construirConBuilderYValidarInfoOK() {
        MiembroPartido miembro = MiembroPartido.builder()
                .usuarioId(UUID.randomUUID())
                .partidoId(UUID.randomUUID())
                .nombreCompleto("Juan Perez")
                .documentoIdentidad("DNI-12345678")
                .lugarInscripcion("Lima")
                .build();

        assertDoesNotThrow(miembro::validateInfo);
        assertEquals(EstadoMiembro.ACTIVO, miembro.getEstado());
        assertFalse(miembro.isVerificado());
    }

    @Test
    void usuarioIdNuloLanzaDatosInvalidosException() {
        MiembroPartido miembro = MiembroPartido.builder()
                .usuarioId(null)
                .partidoId(UUID.randomUUID())
                .nombreCompleto("Juan Perez")
                .documentoIdentidad("DNI-12345678")
                .lugarInscripcion("Lima")
                .build();

        assertThrows(DatosInvalidosException.class, miembro::validateInfo);
    }

    @Test
    void partidoIdNuloLanzaDatosInvalidosException() {
        MiembroPartido miembro = MiembroPartido.builder()
                .usuarioId(UUID.randomUUID())
                .partidoId(null)
                .nombreCompleto("Juan Perez")
                .documentoIdentidad("DNI-12345678")
                .lugarInscripcion("Lima")
                .build();

        assertThrows(DatosInvalidosException.class, miembro::validateInfo);
    }

    @Test
    void snapshotIncompletoLanzaDatosInvalidosException() {
        MiembroPartido miembro = MiembroPartido.builder()
                .usuarioId(UUID.randomUUID())
                .partidoId(UUID.randomUUID())
                .nombreCompleto("")
                .documentoIdentidad("DNI-12345678")
                .lugarInscripcion("Lima")
                .build();

        assertThrows(DatosInvalidosException.class, miembro::validateInfo);
    }

    @Test
    void documentoIdentidadNuloLanzaDatosInvalidosException() {
        MiembroPartido miembro = MiembroPartido.builder()
                .usuarioId(UUID.randomUUID())
                .partidoId(UUID.randomUUID())
                .nombreCompleto("Juan Perez")
                .documentoIdentidad(null)
                .lugarInscripcion("Lima")
                .build();

        assertThrows(DatosInvalidosException.class, miembro::validateInfo);
    }

    @Test
    void lugarInscripcionNuloLanzaDatosInvalidosException() {
        MiembroPartido miembro = MiembroPartido.builder()
                .usuarioId(UUID.randomUUID())
                .partidoId(UUID.randomUUID())
                .nombreCompleto("Juan Perez")
                .documentoIdentidad("DNI-12345678")
                .lugarInscripcion(null)
                .build();

        assertThrows(DatosInvalidosException.class, miembro::validateInfo);
    }

    @Test
    void changeFotoActualizaFotoUrl() {
        MiembroPartido miembro = MiembroPartido.builder()
                .usuarioId(UUID.randomUUID())
                .partidoId(UUID.randomUUID())
                .nombreCompleto("Juan Perez")
                .documentoIdentidad("DNI-12345678")
                .lugarInscripcion("Lima")
                .fotoUrl("https://foto.com/old.jpg")
                .build();

        miembro.changeFoto("https://foto.com/new.jpg");

        assertEquals("https://foto.com/new.jpg", miembro.getFotoUrl());
    }

    @Test
    void desactivarMiembroActivoCambiaEstado() {
        MiembroPartido miembro = MiembroPartido.builder()
                .usuarioId(UUID.randomUUID())
                .partidoId(UUID.randomUUID())
                .nombreCompleto("Juan Perez")
                .documentoIdentidad("DNI-12345678")
                .lugarInscripcion("Lima")
                .build();

        miembro.desactivar("Baja voluntaria");

        assertEquals(EstadoMiembro.INACTIVO, miembro.getEstado());
        assertEquals("Baja voluntaria", miembro.getMotivoBaja());
    }

    @Test
    void desactivarMiembroInactivoLanzaMiembroYaInactivoException() {
        MiembroPartido miembro = MiembroPartido.builder()
                .usuarioId(UUID.randomUUID())
                .partidoId(UUID.randomUUID())
                .nombreCompleto("Juan Perez")
                .documentoIdentidad("DNI-12345678")
                .lugarInscripcion("Lima")
                .estado(EstadoMiembro.INACTIVO)
                .build();

        assertThrows(MiembroYaInactivoException.class, () -> miembro.desactivar("Intento"));
    }

    @Test
    void reactivarCambiaEstadoAActivo() {
        MiembroPartido miembro = MiembroPartido.builder()
                .usuarioId(UUID.randomUUID())
                .partidoId(UUID.randomUUID())
                .nombreCompleto("Juan Perez")
                .documentoIdentidad("DNI-12345678")
                .lugarInscripcion("Lima")
                .estado(EstadoMiembro.INACTIVO)
                .motivoBaja("Baja previa")
                .build();

        miembro.reactivar();

        assertEquals(EstadoMiembro.ACTIVO, miembro.getEstado());
        assertNull(miembro.getMotivoBaja());
    }

    @Test
    void marcarVerificadoCambiaFlag() {
        MiembroPartido miembro = MiembroPartido.builder()
                .usuarioId(UUID.randomUUID())
                .partidoId(UUID.randomUUID())
                .nombreCompleto("Juan Perez")
                .documentoIdentidad("DNI-12345678")
                .lugarInscripcion("Lima")
                .build();

        assertFalse(miembro.isVerificado());
        miembro.marcarVerificado();
        assertTrue(miembro.isVerificado());
    }

    @Test
    void actualizarSnapshotCambiaDatosIdentidad() {
        MiembroPartido miembro = MiembroPartido.builder()
                .usuarioId(UUID.randomUUID())
                .partidoId(UUID.randomUUID())
                .nombreCompleto("Juan Perez")
                .documentoIdentidad("DNI-12345678")
                .lugarInscripcion("Lima")
                .build();

        miembro.actualizarSnapshot("Juan Actualizado", "DNI-87654321", "Arequipa");

        assertEquals("Juan Actualizado", miembro.getNombreCompleto());
        assertEquals("DNI-87654321", miembro.getDocumentoIdentidad());
        assertEquals("Arequipa", miembro.getLugarInscripcion());
    }
}
