package org.infoUFPS.DTOs.Requests;

import org.infoUFPS.Enums.TipoMateria;

public record AdminSaveMateriaCommand(
        String nombre,
        String codigo,
        Integer creditos,
        TipoMateria tipo
) {
}

