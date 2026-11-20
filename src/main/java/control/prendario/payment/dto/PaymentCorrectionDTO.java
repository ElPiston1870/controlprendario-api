package control.prendario.payment.dto;

import control.prendario.payment.domain.Payment;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Datos corregidos del pago y motivo de la corrección")
public class PaymentCorrectionDTO {

    @NotNull
    @Positive
    @Schema(description = "Monto correcto del pago", example = "125000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("montoPagado")
    private BigDecimal amountPaid;

    @NotNull
    @Schema(description = "Concepto del pago corregido", example = "CAPITAL", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("tipoPago")
    private Payment.PaymentType paymentType;

    @NotNull
    @Schema(description = "Método de pago correcto", example = "EFECTIVO", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("metodoPago")
    private Payment.PaymentMethod paymentMethod;

    @Size(max = 2000)
    @Schema(description = "Observaciones del pago corregido", example = "Pago registrado con el monto equivocado")
    @JsonProperty("observaciones")
    private String notes;

    @NotBlank
    @Size(max = 1000)
    @Schema(description = "Motivo por el que se corrige el pago", example = "Error de digitación", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("motivo")
    private String reason;
}
