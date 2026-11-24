package control.prendario.shared.query;

import java.util.List;
import org.springframework.data.domain.Page;

/** Stable response for the Angular tables and lookup controls. */
public record PageResult<T>(List<T> content, int number, int size, long totalElements, int totalPages) {
    public static <T> PageResult<T> from(Page<T> page) {
        return new PageResult<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
