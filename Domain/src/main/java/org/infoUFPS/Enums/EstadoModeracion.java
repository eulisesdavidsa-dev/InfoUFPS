package org.infoUFPS.Enums;

import org.infoUFPS.Exceptions.DomainException;

public enum EstadoModeracion {
    EN_REVISION {
        @Override
        public boolean canTransitionTo(EstadoModeracion next) {
            return next == PUBLICADO || next == ARCHIVADO;
        }
    },
    PUBLICADO {
        @Override
        public boolean canTransitionTo(EstadoModeracion next) {
            return next == ARCHIVADO;
        }
    },
    ARCHIVADO {
        @Override
        public boolean canTransitionTo(EstadoModeracion next) {
            return false;
        }
    };

    public abstract boolean canTransitionTo(EstadoModeracion next);

    public static EstadoModeracion fromString(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        for (EstadoModeracion e : values()) {
            if (e.name().equalsIgnoreCase(text.trim())) {
                return e;
            }
        }
        throw new DomainException("Estado desconocido: " + text);
    }
}

