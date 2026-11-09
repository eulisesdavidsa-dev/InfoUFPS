package org.infoUFPS.Criterias;

import org.infoUFPS.Enums.EstadoModeracion;
import org.infoUFPS.Enums.TipoMateria;

public record MateriaSearchCriteria(
        String query,
        Integer creditos,
        TipoMateria tipo,
        EstadoModeracion estado
) {
    public static MateriaSearchCriteria publicSearch(String query, Integer creditos, TipoMateria tipo) {
        return new MateriaSearchCriteria(query, creditos, tipo, EstadoModeracion.PUBLICADO);
    }
}

