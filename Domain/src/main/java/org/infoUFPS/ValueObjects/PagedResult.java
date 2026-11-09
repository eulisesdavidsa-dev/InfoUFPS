package org.infoUFPS.ValueObjects;

import java.util.List;
import java.util.function.Function;

/**
 * Resultado paginado genérico del dominio.
 *
 * @param items      elementos de la página actual.
 * @param total      total de elementos que cumplen el criterio.
 * @param page       página base cero.
 * @param size       tamaño de página.
 * @param totalPages total de páginas calculado.
 * @param <T>        tipo de elemento.
 */
public record PagedResult<T>(
        List<T> items,
        long total,
        int page,
        int size,
        int totalPages
) {

    public PagedResult {
        items = items == null ? List.of() : List.copyOf(items);
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = PageRequest.DEFAULT_SIZE;
        }
        long calculatedPages = size == 0 ? 0 : (total + size - 1) / size;
        totalPages = (int) calculatedPages;
    }

    /**
     * Construye un resultado paginado a partir de la página y el total.
     *
     * @param items elementos de la página.
     * @param total total de elementos.
     * @param request solicitud de paginación.
     * @return resultado paginado.
     */
    public static <T> PagedResult<T> of(List<T> items, long total, PageRequest request) {
        PageRequest normalized = request != null ? request : PageRequest.first();
        return new PagedResult<>(items, total, normalized.page(), normalized.size(), 0);
    }

    /**
     * Transforma los elementos manteniendo la metadata de paginación.
     *
     * @param mapper función de transformación.
     * @param <R>    tipo destino.
     * @return resultado paginado transformado.
     */
    public <R> PagedResult<R> map(Function<? super T, ? extends R> mapper) {
        java.util.List<R> mapped = new java.util.ArrayList<>(items.size());
        for (T item : items) {
            mapped.add(mapper.apply(item));
        }
        return new PagedResult<>(mapped, total, page, size, totalPages);
    }

    /**
     * @return true si existe una página siguiente.
     */
    public boolean hasNext() {
        return page + 1 < totalPages;
    }
}
