package com.safevoting.elecciones.unit.domain.model.partido;

import com.safevoting.elecciones.domain.exception.DatosInvalidosException;
import com.safevoting.elecciones.domain.exception.partido.PartidoYaInhabilitadoException;
import com.safevoting.elecciones.domain.model.partido.EstadoPartido;
import com.safevoting.elecciones.domain.model.partido.PartidoPolitico;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PartidoPoliticoTest {

    @Test
    void constructorWithBuilderAndValidateInfoShouldSucceed() {
        PartidoPolitico partido = PartidoPolitico.builder()
                .id(UUID.randomUUID())
                .nombre("PARTIDO TEST")
                .descripcion("Descripcion del partido")
                .estado(EstadoPartido.HABILITADO)
                .build();

        assertNotNull(partido);
        partido.validateInfo();
    }

    @Test
    void nombreVacioShouldThrowDatosInvalidosException() {
        PartidoPolitico partido = PartidoPolitico.builder()
                .nombre("")
                .estado(EstadoPartido.HABILITADO)
                .build();

        assertThrows(DatosInvalidosException.class, partido::validateInfo);
    }

    @Test
    void nombreGreaterThan100ShouldThrowDatosInvalidosException() {
        PartidoPolitico partido = PartidoPolitico.builder()
                .nombre("A".repeat(101))
                .estado(EstadoPartido.HABILITADO)
                .build();

        assertThrows(DatosInvalidosException.class, partido::validateInfo);
    }

    @Test
    void nombreWithLowerCaseShouldThrowDatosInvalidosException() {
        PartidoPolitico partido = PartidoPolitico.builder()
                .nombre("Partido Minusculas")
                .estado(EstadoPartido.HABILITADO)
                .build();

        assertThrows(DatosInvalidosException.class, partido::validateInfo);
    }

    @Test
    void setNombreShouldUpdateNombre() {
        PartidoPolitico partido = PartidoPolitico.builder()
                .nombre("PARTIDO ORIGINAL")
                .descripcion("Descripcion original")
                .estado(EstadoPartido.HABILITADO)
                .build();

        partido.setNombre("PARTIDO EDITADO");

        assertEquals("PARTIDO EDITADO", partido.getNombre());
    }

    @Test
    void setNombreWithInvalidNombreShouldThrowDatosInvalidosException() {
        PartidoPolitico partido = PartidoPolitico.builder()
                .nombre("PARTIDO ORIGINAL")
                .estado(EstadoPartido.HABILITADO)
                .build();

        assertThrows(DatosInvalidosException.class,
                () -> partido.setNombre("partido minusculas"));
    }

    @Test
    void setDescripcionShouldUpdateDescripcion() {
        PartidoPolitico partido = PartidoPolitico.builder()
                .nombre("PARTIDO TEST")
                .descripcion("Descripcion original")
                .estado(EstadoPartido.HABILITADO)
                .build();

        partido.setDescripcion("Nueva descripcion");

        assertEquals("Nueva descripcion", partido.getDescripcion());
    }

    @Test
    void inhabilitarShouldChangeEstado() {
        PartidoPolitico partido = PartidoPolitico.builder()
                .nombre("PARTIDO TEST")
                .estado(EstadoPartido.HABILITADO)
                .build();

        partido.inhabilitar();

        assertEquals(EstadoPartido.INHABILITADO, partido.getEstado());
    }

    @Test
    void inhabilitarAlreadyInhabilitadoShouldThrowPartidoYaInhabilitadoException() {
        PartidoPolitico partido = PartidoPolitico.builder()
                .id(UUID.randomUUID())
                .nombre("PARTIDO TEST")
                .estado(EstadoPartido.INHABILITADO)
                .build();

        assertThrows(PartidoYaInhabilitadoException.class, partido::inhabilitar);
    }
}
