package control.prendario.collateral;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class CollateralInventoryDTO {

    @JsonProperty("tipoGarantia")
    private String collateralType;
    @JsonProperty("idPrestamo")
    private Long loanId;
    @JsonProperty("idCliente")
    private Long clientId;
    @JsonProperty("nombreCliente")
    private String clientName;
    @JsonProperty("identificador")
    private String identifier;
    @JsonProperty("descripcion")
    private String description;
    @JsonProperty("montoPrestamo")
    private BigDecimal loanAmount;
    @JsonProperty("capitalPendiente")
    private BigDecimal pendingPrincipal;
    @JsonProperty("estadoPrestamo")
    private String loanStatus;
    @JsonProperty("fechaPrestamo")
    private LocalDateTime loanDate;
    @JsonProperty("fechaVencimiento")
    private String dueDate;
    @JsonProperty("enGarantia")
    private boolean inCollateral;
}
