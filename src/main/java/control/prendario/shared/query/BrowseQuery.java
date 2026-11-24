package control.prendario.shared.query;

import java.time.LocalDate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;

public record BrowseQuery(
        Integer pagina, Integer tamano, String termino, String nombre, String nombreCliente,
        String numeroDocumento, Long id, String estado,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vencimiento,
        String tipoPago, String metodoPago, String tipoGarantia) {

    public PageRequest page(Sort sort) {
        return page(pagina, tamano, sort);
    }

    public static PageRequest page(Integer number, Integer size, Sort sort) {
        int safeSize = size == null ? 20 : Math.max(1, Math.min(size, 100));
        int safeNumber = number == null ? 0 : Math.max(0, number);
        if ((long) safeNumber * safeSize > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("El número de página es demasiado alto.");
        }
        return PageRequest.of(safeNumber, safeSize, sort);
    }

    public String clientName() {
        return CatalogFilters.hasText(nombre) ? nombre : nombreCliente;
    }
}
