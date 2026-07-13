package com.safevoting.elecciones.infrastructure.adapter.in.rest;

import com.safevoting.elecciones.domain.exception.DatosInvalidosException;
import com.safevoting.elecciones.domain.exception.miembro.MiembroDuplicadoException;
import com.safevoting.elecciones.domain.exception.miembro.MiembroInscritoEnVotacionException;
import com.safevoting.elecciones.domain.exception.miembro.MiembroNoEncontradoException;
import com.safevoting.elecciones.domain.exception.miembro.MiembroYaInactivoException;
import com.safevoting.elecciones.domain.exception.miembro.UsuarioNoEncontradoException;
import com.safevoting.elecciones.domain.exception.partido.NombreDuplicadoException;
import com.safevoting.elecciones.domain.exception.partido.PartidoConCandidatosEnVotacionException;
import com.safevoting.elecciones.domain.exception.partido.PartidoNoEncontradoException;
import com.safevoting.elecciones.domain.exception.partido.PartidoYaInhabilitadoException;
import com.safevoting.elecciones.domain.exception.votacion.AlcanceExcedidoException;
import com.safevoting.elecciones.domain.exception.votacion.FechaInicioFuturaException;
import com.safevoting.elecciones.domain.exception.votacion.MotivoRequeridoException;
import com.safevoting.elecciones.domain.exception.votacion.SinCandidatosException;
import com.safevoting.elecciones.domain.exception.votacion.TipoAlcanceIncompatibleException;
import com.safevoting.elecciones.domain.exception.votacion.TransicionEstadoInvalidaException;
import com.safevoting.elecciones.domain.exception.votacion.VotacionNoEncontradaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NombreDuplicadoException.class)
    public ResponseEntity<ApiErrorResponse> handleNombreDuplicado(NombreDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(PartidoNoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handlePartidoNoEncontrado(PartidoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(PartidoYaInhabilitadoException.class)
    public ResponseEntity<ApiErrorResponse> handlePartidoYaInhabilitado(PartidoYaInhabilitadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(PartidoConCandidatosEnVotacionException.class)
    public ResponseEntity<ApiErrorResponse> handlePartidoConCandidatosEnVotacion(PartidoConCandidatosEnVotacionException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<ApiErrorResponse> handleDatosInvalidos(DatosInvalidosException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(MiembroNoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleMiembroNoEncontrado(MiembroNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(MiembroDuplicadoException.class)
    public ResponseEntity<ApiErrorResponse> handleMiembroDuplicado(MiembroDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(MiembroYaInactivoException.class)
    public ResponseEntity<ApiErrorResponse> handleMiembroYaInactivo(MiembroYaInactivoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleUsuarioNoEncontrado(UsuarioNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(MiembroInscritoEnVotacionException.class)
    public ResponseEntity<ApiErrorResponse> handleMiembroInscritoEnVotacion(MiembroInscritoEnVotacionException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(VotacionNoEncontradaException.class)
    public ResponseEntity<ApiErrorResponse> handleVotacionNoEncontrada(VotacionNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(com.safevoting.elecciones.domain.exception.votacion.NombreDuplicadoException.class)
    public ResponseEntity<ApiErrorResponse> handleNombreVotacionDuplicado(com.safevoting.elecciones.domain.exception.votacion.NombreDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(TransicionEstadoInvalidaException.class)
    public ResponseEntity<ApiErrorResponse> handleTransicionEstadoInvalida(TransicionEstadoInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(TipoAlcanceIncompatibleException.class)
    public ResponseEntity<ApiErrorResponse> handleTipoAlcanceIncompatible(TipoAlcanceIncompatibleException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(SinCandidatosException.class)
    public ResponseEntity<ApiErrorResponse> handleSinCandidatos(SinCandidatosException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(FechaInicioFuturaException.class)
    public ResponseEntity<ApiErrorResponse> handleFechaInicioFutura(FechaInicioFuturaException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(MotivoRequeridoException.class)
    public ResponseEntity<ApiErrorResponse> handleMotivoRequerido(MotivoRequeridoException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(AlcanceExcedidoException.class)
    public ResponseEntity<ApiErrorResponse> handleAlcanceExcedido(AlcanceExcedidoException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(WebExchangeBindException ex) {
        String message = ex.getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Error de validación");
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiErrorResponse.of("VALIDATION_ERROR", message));
    }
}
