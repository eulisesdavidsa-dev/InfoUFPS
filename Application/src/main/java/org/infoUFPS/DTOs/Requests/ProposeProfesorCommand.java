package org.infoUFPS.DTOs.Requests;

import org.infoUFPS.Enums.Facultad;

import java.util.Set;

public record ProposeProfesorCommand(
        String nombre,
        Facultad facultad,
        String email,
        String proponenteId
) {
}

