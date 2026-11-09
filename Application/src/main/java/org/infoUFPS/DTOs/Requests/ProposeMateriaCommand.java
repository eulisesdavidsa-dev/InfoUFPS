package org.infoUFPS.DTOs.Requests;

import org.infoUFPS.Enums.TipoMateria;

public record ProposeMateriaCommand(
        String nombre,
        String codigo,
        Integer creditos,
        TipoMateria tipo,
        String proponenteId
) {
}

