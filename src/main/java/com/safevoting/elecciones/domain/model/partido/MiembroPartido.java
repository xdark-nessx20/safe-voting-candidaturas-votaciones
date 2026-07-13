package com.safevoting.elecciones.domain.model.partido;

import com.safevoting.elecciones.domain.exception.DatosInvalidosException;
import com.safevoting.elecciones.domain.exception.miembro.MiembroYaActivoException;
import com.safevoting.elecciones.domain.exception.miembro.MiembroYaInactivoException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiembroPartido {

    private UUID id;
    private UUID usuarioId;
    private UUID partidoId;
    private String nombreCompleto;
    private String documentoIdentidad;
    private String lugarInscripcion;
    private String fotoUrl;
    @Builder.Default
    private EstadoMiembro estado = EstadoMiembro.ACTIVO;
    @Builder.Default
    private boolean verificado = false;
    private String motivoBaja;

    public void validateInfo() {
        validateUsuarioPartido();
        validateSnapshot();
    }

    private void validateUsuarioPartido() {
        if (usuarioId == null || partidoId == null) {
            throw new DatosInvalidosException("usuarioId y partidoId son obligatorios");
        }
    }

    private void validateSnapshot() {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            throw new DatosInvalidosException("nombreCompleto es obligatorio");
        }
        if (documentoIdentidad == null || documentoIdentidad.isBlank()) {
            throw new DatosInvalidosException("documentoIdentidad es obligatorio");
        }
        if (lugarInscripcion == null || lugarInscripcion.isBlank()) {
            throw new DatosInvalidosException("lugarInscripcion es obligatorio");
        }
    }

    public boolean isActivo() {
        return EstadoMiembro.ACTIVO.equals(this.estado);
    }

    public boolean isInactivo() {
        return EstadoMiembro.INACTIVO.equals(this.estado);
    }

    public void changeFoto(String nuevaFotoUrl) {
        this.fotoUrl = nuevaFotoUrl;
    }

    public void desactivar(String motivo) {
        if (isInactivo()) {
            throw new MiembroYaInactivoException(this.id);
        }
        this.estado = EstadoMiembro.INACTIVO;
        this.motivoBaja = motivo;
    }

    public void reactivar() {
        if (isActivo())
            throw new MiembroYaActivoException(this.id);
        this.estado = EstadoMiembro.ACTIVO;
        this.motivoBaja = null;
    }

    public void marcarVerificado() {
        this.verificado = true;
    }

    public void actualizarSnapshot(String nombre, String documento, String lugar) {
        this.nombreCompleto = nombre;
        this.documentoIdentidad = documento;
        this.lugarInscripcion = lugar;
        validateSnapshot();
    }
}
