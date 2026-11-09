package org.infoUFPS.ValueObjects;

/**
 * Solicitud de paginación base cero.
 * Normaliza valores fuera de rango para evitar consultas inválidas.
 */
public record PageRequest(int page, int size) {

    /** Tamaño de página por defecto. */
    public static final int DEFAULT_SIZE = 20;

    /** Tamaño máximo permitido por página. */
    public static final int MAX_SIZE = 100;

    public PageRequest {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = DEFAULT_SIZE;
        }
        if (size > MAX_SIZE) {
            size = MAX_SIZE;
        }
    }

    /**
     * Crea una solicitud de paginación normalizada.
     *
     * @param page página base cero.
     * @param size tamaño de página.
     * @return solicitud normalizada.
     */
    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size);
    }

    /**
     * @return solicitud con página 0 y tamaño por defecto.
     */
    public static PageRequest first() {
        return new PageRequest(0, DEFAULT_SIZE);
    }

    /**
     * @return desplazamiento usado por el repositorio.
     */
    public long offset() {
        return (long) page * size;
    }
}
