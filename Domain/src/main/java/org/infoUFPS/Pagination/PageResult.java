package org.infoUFPS.Pagination;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public record PageResult<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PageResult<T> of(List<T> items, int page, int size, long totalElements) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        return new PageResult<>(
                items != null ? items : Collections.emptyList(),
                page,
                size,
                totalElements,
                totalPages
        );
    }

    public static <T> PageResult<T> empty(int page, int size) {
        return new PageResult<>(Collections.emptyList(), page, size, 0L, 0);
    }

    public <R> PageResult<R> map(Function<T, R> mapper) {
        List<R> mappedItems = this.items.stream().map(mapper).toList();
        return new PageResult<>(mappedItems, this.page, this.size, this.totalElements, this.totalPages);
    }
}

