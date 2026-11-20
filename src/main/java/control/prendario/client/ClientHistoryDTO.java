package control.prendario.client;

import control.prendario.loan.Loan;
import control.prendario.loan.MachineryLoan;
import control.prendario.payment.MachineryPayment;
import control.prendario.payment.Payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "Historial consolidado de préstamos y pagos de un cliente")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientHistoryDTO {

    @Schema(description = "Datos generales del cliente")
    @JsonProperty("cliente")
    private Client client;

    @Schema(description = "Préstamos vehiculares asociados al cliente")
    @JsonProperty("prestamosVehiculares")
    private List<Loan> vehicleLoans;

    @Schema(description = "Préstamos de maquinaria asociados al cliente")
    @JsonProperty("prestamosMaquinaria")
    private List<MachineryLoan> machineryLoans;

    @Schema(description = "Pagos realizados sobre préstamos vehiculares")
    @JsonProperty("pagosVehiculares")
    private List<Payment> vehiclePayments;

    @Schema(description = "Pagos realizados sobre préstamos de maquinaria")
    @JsonProperty("pagosMaquinaria")
    private List<MachineryPayment> machineryPayments;
}
