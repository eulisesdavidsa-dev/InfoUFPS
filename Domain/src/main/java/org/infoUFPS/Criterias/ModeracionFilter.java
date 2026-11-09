package org.infoUFPS.Criterias;

import org.infoUFPS.Enums.EstadoModeracion;

public record ModeracionFilter(
        EstadoModeracion estado
) {
    public static ModeracionFilter of(EstadoModeracion estado) {
        return new ModeracionFilter(estado);
    }
}

