package com.interview_scheduler.backend.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {

    private List<T> data;

    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;

    private Long nextCursor;
    private Long prevCursor;
    private Boolean hasNext;
    private Boolean hasPrevious;

    public static <T> PaginatedResponse<T> ofOffset(List<T> data, int page, int size,
            long totalElements, int totalPages) {
        return PaginatedResponse.<T>builder()
                .data(data)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();
    }

    public static <T> PaginatedResponse<T> ofCursor(List<T> data, Long nextCursor,
            Long prevCursor, boolean hasNext) {
        return PaginatedResponse.<T>builder()
                .data(data)
                .nextCursor(nextCursor)
                .prevCursor(prevCursor)
                .hasNext(hasNext)
                .hasPrevious(prevCursor != null && prevCursor > 0)
                .build();
    }
}
