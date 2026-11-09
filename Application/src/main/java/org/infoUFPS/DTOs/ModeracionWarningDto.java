package org.infoUFPS.DTOs;

public record ModeracionWarningDto(
        String id,
        String nombre,
        String estado,
        double similitud,
        String mensaje
) {
}

