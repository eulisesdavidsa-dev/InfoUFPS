package org.infoUFPS.Criterias;

import org.infoUFPS.Enums.EstadoModeracion;
import org.infoUFPS.Enums.Facultad;

public record ProfesorSearchCriteria(
        String query,
        Facultad facultad,
        EstadoModeracion estado
) {
    public static ProfesorSearchCriteria publicSearch(String query, Facultad facultad) {
        return new ProfesorSearchCriteria(query, facultad, EstadoModeracion.PUBLICADO);
    }
}

