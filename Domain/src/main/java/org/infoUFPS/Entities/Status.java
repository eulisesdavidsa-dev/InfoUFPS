package org.infoUFPS.Entities;

/**
 * Enum que representa los estados del ciclo de vida de un contenido en el sistema.
 * Modela una máquina de estados determinista donde cada estado define explícitamente
 * las transiciones válidas hacia otros estados.
 */
public enum Status {

    /**
     * Estado inicial de moderación. El contenido está pendiente de revisión por un administrador.
     * Transiciones permitidas: Aprobado (PUBLISHED) o Rechazado/Archivado directo (ARCHIVED).
     */
    UNDER_REVIEW {
        @Override
        public boolean canTransitionTo(Status nextStatus) {
            return nextStatus == PUBLISHED || nextStatus == ARCHIVED;
        }
    },

    /**
     * Estado público. El contenido fue aprobado y es visible en el feed para los usuarios.
     * Transiciones permitidas: Retirado/Deshabilitado (ARCHIVED).
     */
    PUBLISHED {
        @Override
        public boolean canTransitionTo(Status nextStatus) {
            return nextStatus == ARCHIVED;
        }
    },

    /**
     * Estado final o retirado. El contenido fue ocultado o rechazado.
     * Transiciones permitidas: Ninguna (Estado terminal).
     */
    ARCHIVED {
        @Override
        public boolean canTransitionTo(Status nextStatus) {
            return false;
        }
    };

    /**
     * Evalúa si es legal hacer la transición desde el estado actual hacia el estado destino.
     *
     * @param nextStatus El estado objetivo al que se desea pasar.
     * @return true si la transición está permitida por las reglas del ciclo de vida; false de lo contrario.
     */
    public abstract boolean canTransitionTo(Status nextStatus);
}
