package org.infoUFPS.DTOs.Requests;

import org.infoUFPS.Enums.Facultad;
import org.infoUFPS.Enums.TipoMateria;

public record ModerateEntityCommand(
        // Correcciones opcionales para Profesor
        String nuevoNombre,
        Facultad nuevaFacultad,
        String nuevoEmail,
        // Correcciones opcionales para Materia
        String nuevoCodigo,
        Integer nuevosCreditos,
        TipoMateria nuevoTipo
) {
    public static ModerateEntityCommand empty() {
        return new ModerateEntityCommand(null, null, null, null, null, null);
    }
}

