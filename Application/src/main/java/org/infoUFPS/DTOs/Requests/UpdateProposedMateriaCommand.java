package org.infoUFPS.DTOs.Requests;

import org.infoUFPS.Enums.TipoMateria;

public record UpdateProposedMateriaCommand(
        String materiaId,
        String nombre,
        String codigo,
        Integer creditos,
        TipoMateria tipo,
        String editorId
) {
}

