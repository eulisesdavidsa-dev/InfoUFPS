package org.infoUFPS.DTOs.Requests;

import org.infoUFPS.Enums.TipoDestino;

public record CreateResenaCommand(
        String autorId,
        TipoDestino tipoDestino,
        String profesorId,
        String materiaId,
        int calificacion,
        String comentario,
        String materiaCursada
) {
}

