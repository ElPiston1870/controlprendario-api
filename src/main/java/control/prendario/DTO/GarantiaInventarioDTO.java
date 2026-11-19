package control.prendario.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Garantía registrada y su préstamo asociado")
public class GarantiaInventarioDTO {

    private String tipoGarantia;
    private Long idPrestamo;
    private Long idCliente;
    private String nombreCliente;
    private String identificador;
    private String descripcion;
    private BigDecimal montoPrestamo;
    private BigDecimal capitalPendiente;
    private String estadoPrestamo;
    private LocalDateTime fechaPrestamo;
    private String fechaVencimiento;
    private boolean enGarantia;
}
