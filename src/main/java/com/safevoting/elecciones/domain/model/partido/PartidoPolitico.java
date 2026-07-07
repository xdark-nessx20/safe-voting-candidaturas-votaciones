package com.safevoting.elecciones.domain.model.partido;

import com.safevoting.elecciones.domain.exception.DatosInvalidosException;
import com.safevoting.elecciones.domain.exception.UrlInvalidaException;
import com.safevoting.elecciones.domain.exception.partido.PartidoYaInhabilitadoException;
import lombok.*;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartidoPolitico {

    private UUID id;
    private String nombre;
    @Setter
    private String descripcion;
    private String logoUrl;

    @Builder.Default
    private EstadoPartido estado = EstadoPartido.HABILITADO;

    public void validateInfo() {
        validateNombre();
    }

    private void validateNombre() {
        if (nombreVacio()) {
            throw new DatosInvalidosException("El nombre del partido no puede estar vacío");
        }
        if (nombreMuyLargo()) {
            throw new DatosInvalidosException("El nombre del partido no puede exceder los 100 caracteres");
        }
        if (nombreDontMatch()) {
            throw new DatosInvalidosException("El nombre del partido solo puede contener letras mayúsculas (A-Z) y espacios");
        }
    }

    private boolean nombreVacio() {
        return nombre == null || nombre.isBlank();
    }

    private boolean nombreMuyLargo() {
        return nombre.length() > 100;
    }

    private boolean nombreDontMatch() {
        return !nombre.matches("^[A-Z ]+$");
    }

    public void setNombre(String newNombre) {
        this.nombre = newNombre;
        validateNombre();
    }

    public void changeLogo(String newLogoUrl) {
        if (newLogoUrl != null && !newLogoUrl.startsWith("https://")) {
            throw new UrlInvalidaException("La url del logo es invalida.");
        }
        this.logoUrl = newLogoUrl;
    }

    public void inhabilitar() {
        if (estado.esInhabilitado()) {
            throw new PartidoYaInhabilitadoException(id);
        }
        this.estado = EstadoPartido.INHABILITADO;
    }
}
