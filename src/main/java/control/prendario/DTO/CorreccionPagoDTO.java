package control.prendario.DTO;

import control.prendario.model.Pago;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Datos corregidos del pago y motivo de la corrección")
public class CorreccionPagoDTO {

    @NotNull
    @Positive
    @Schema(description = "Monto correcto del pago", example = "125000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal montoPagado;

    @NotNull
    @Schema(description = "Concepto del pago corregido", example = "CAPITAL", requiredMode = Schema.RequiredMode.REQUIRED)
    private Pago.TipoPago tipoPago;

    @NotNull
    @Schema(description = "Método de pago correcto", example = "EFECTIVO", requiredMode = Schema.RequiredMode.REQUIRED)
    private Pago.MetodoPago metodoPago;

    @Size(max = 2000)
    @Schema(description = "Observaciones del pago corregido", example = "Pago registrado con el monto equivocado")
    private String observaciones;

    @NotBlank
    @Size(max = 1000)
    @Schema(description = "Motivo por el que se corrige el pago", example = "Error de digitación", requiredMode = Schema.RequiredMode.REQUIRED)
    private String motivo;
}
