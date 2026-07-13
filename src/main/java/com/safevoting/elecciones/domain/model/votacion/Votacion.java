package com.safevoting.elecciones.domain.model.votacion;

import com.safevoting.elecciones.domain.exception.DatosInvalidosException;
import com.safevoting.elecciones.domain.exception.votacion.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Votacion {

    private UUID id;
    private String nombre;
    private TipoVotacion tipo;
    private AlcanceVotacion alcance;
    private UUID departamentoId;
    private UUID municipioId;
    private Instant fechaInicio;
    private Instant fechaFin;
    @Builder.Default
    private EstadoVotacion estado = EstadoVotacion.ACTIVA;
    private String motivo;

    public boolean isActiva() {
        return EstadoVotacion.ACTIVA.equals(this.estado);
    }

    public boolean isEnProgreso() {
        return EstadoVotacion.EN_PROGRESO.equals(this.estado);
    }

    public boolean isFinalizada() {
        return EstadoVotacion.FINALIZADA.equals(this.estado);
    }

    public boolean isCompletada() {
        return EstadoVotacion.COMPLETADA.equals(this.estado);
    }

    public boolean isCancelada() {
        return EstadoVotacion.CANCELADA.equals(this.estado);
    }

    public void validateInfo() {
        validateNombre();
        validateTipoAlcance();
        validateIdsPorAlcance();
    }

    private void validateNombre() {
        if (nombre == null || nombre.isBlank()) {
            throw new DatosInvalidosException("El nombre de la votación no puede estar vacío");
        }
    }

    private void validateTipoAlcance() {
        if (tipo == null) {
            throw new DatosInvalidosException("El tipo de votación es obligatorio");
        }
        if (alcance == null) {
            throw new DatosInvalidosException("El alcance de la votación es obligatorio");
        }
    }

    private void validateIdsPorAlcance() {
        if (alcance == AlcanceVotacion.MUNICIPAL && municipioId == null) {
            throw new DatosInvalidosException("municipioId es obligatorio para votaciones de alcance MUNICIPAL");
        }
        if (alcance == AlcanceVotacion.DEPARTAMENTAL && departamentoId == null) {
            throw new DatosInvalidosException("departamentoId es obligatorio para votaciones de alcance DEPARTAMENTAL");
        }
    }

    public void abrir(boolean tieneCandidatos) {
        validarActiva();
        validarTieneCandidatos(tieneCandidatos);
        validarFechaInicioCercana();

        this.estado = EstadoVotacion.EN_PROGRESO;
    }

    private void validarFechaInicioCercana() {
        if (fechaInicio != null && Instant.now().isBefore(fechaInicio)) {
            throw new FechaInicioFuturaException();
        }
    }

    private void validarActiva() {
        if (!isActiva()) {
            throw new TransicionEstadoInvalidaException(this.estado, EstadoVotacion.EN_PROGRESO);
        }
    }

    private void validarTieneCandidatos(boolean tieneCandidatos) {
        if (!tieneCandidatos) {
            throw new SinCandidatosException();
        }
    }

    public void cerrar() {
        validarEnProgreso();
        this.estado = EstadoVotacion.FINALIZADA;
    }

    private void validarEnProgreso() {
        if (!isEnProgreso()) {
            throw new TransicionEstadoInvalidaException(this.estado, EstadoVotacion.FINALIZADA);
        }
    }

    public void cancelar(String motivoCancelacion) {
        validarActivaOEnProgreso();
        validarMotivoCancelacion(motivoCancelacion);

        this.estado = EstadoVotacion.CANCELADA;
        this.motivo = motivoCancelacion;
    }

    private void validarActivaOEnProgreso() {
        if (!isActiva() && !isEnProgreso()) {
            throw new TransicionEstadoInvalidaException(this.estado, EstadoVotacion.CANCELADA);
        }
    }

    private void validarMotivoCancelacion(String motivoCancelacion) {
        if (motivoCancelacion == null || motivoCancelacion.isBlank()) {
            throw new MotivoRequeridoException();
        }
    }

    public void completar() {
        validarNoFinalizada();
        this.estado = EstadoVotacion.COMPLETADA;
    }

    private void validarNoFinalizada() {
        if (!isFinalizada()) {
            throw new TransicionEstadoInvalidaException(this.estado, EstadoVotacion.COMPLETADA);
        }
    }

    public void reactivar() {
        validarNoCancelada();
        this.estado = EstadoVotacion.ACTIVA;
        this.motivo = null;
    }

    private void validarNoCancelada() {
        if (!isCancelada()) {
            throw new TransicionEstadoInvalidaException(this.estado, EstadoVotacion.ACTIVA);
        }
    }

    public void establecerFechas(Instant inicio, Instant fin) {
        validarActivaOCancelada();
        this.fechaInicio = inicio;
        this.fechaFin = fin;
    }

    private void validarActivaOCancelada() {
        if (!isActiva() && !isCancelada()) {
            throw new EstadoInvalidoParaModificarFechaException
                    ("La votacion debe estar activa o cancelada para moficar sus fechas");
        }
    }
}
