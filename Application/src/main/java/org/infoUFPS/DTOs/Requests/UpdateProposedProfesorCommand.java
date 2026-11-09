package org.infoUFPS.DTOs.Requests;

import org.infoUFPS.Enums.Facultad;

public record UpdateProposedProfesorCommand(
        String profesorId,
        String nombre,
        Facultad facultad,
        String email,
        String editorId
) {
}

