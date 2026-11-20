package control.prendario.loan.dto;

import control.prendario.loan.domain.LoanStatus;
import control.prendario.vehicle.dto.VehicleDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Schema(description = "DTO para la creación y actualización de préstamos")
@Data
public class LoanDTO {
    @Schema(description = "ID del cliente asociado al préstamo", example = "1")
    @JsonProperty("idCliente")
    private Long clientId;

    @Schema(description = "Información del vehículo en garantía")
    @JsonProperty("vehiculo")
    private VehicleDTO vehicle;

    @Schema(description = "Monto del préstamo", example = "5000000.00")
    @JsonProperty("montoPrestamo")
    private BigDecimal loanAmount;

    @Schema(description = "Tasa de interés anual", example = "12.5")
    @JsonProperty("tasaInteres")
    private BigDecimal interestRate;

    @Schema(description = "Observaciones adicionales del préstamo",
            example = "Préstamo con garantía de vehículo modelo 2020")
    @JsonProperty("observaciones")
    private String notes;

    @Schema(description = "Estado actual del préstamo", example = "ACTIVO",
            allowableValues = {"ACTIVO", "VENCIDO", "PAGADO", "CANCELADO"})
    @JsonProperty("estadoPrestamo")
    private LoanStatus loanStatus;
}
