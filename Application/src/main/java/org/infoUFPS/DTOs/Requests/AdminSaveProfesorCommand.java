package org.infoUFPS.DTOs.Requests;

import org.infoUFPS.Enums.Facultad;

import java.util.Set;

public record AdminSaveProfesorCommand(
        String nombre,
        Facultad facultad,
        String email,
        Set<String> materiasIds
) {
}

