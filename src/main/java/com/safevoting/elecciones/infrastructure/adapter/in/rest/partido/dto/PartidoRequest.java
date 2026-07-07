package com.safevoting.elecciones.infrastructure.adapter.in.rest.partido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PartidoRequest(
        @NotBlank(message = "El nombre del partido es obligatorio")
        @Size(max = 100, message = "El nombre del partido no puede exceder los 100 caracteres")
        @Pattern(regexp = "^[A-Z ]+$", message = "El nombre del partido solo puede contener letras mayúsculas (A-Z) y espacios")
        String nombre,

        String descripcion,

        String logoBase64
) {
}
