package control.prendario.movement;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Entidad que representa un movimiento financiero")
@Entity
@Table(name = "movimientos")
@Data
public class Movement {
    @Schema(description = "Identificador único del movimiento", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    @JsonProperty("idMovimiento")
    private Long movementId;

    @Schema(description = "Fecha y hora del movimiento")
    @Column(name = "fecha_movimiento")
    @JsonProperty("fechaMovimiento")
    private LocalDateTime movementDate;

    @Schema(description = "Tipo de movimiento",
            allowableValues = {"ENTRADA", "SALIDA"},
            example = "ENTRADA")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false)
    @JsonProperty("tipoMovimiento")
    private MovementType movementType;

    @Schema(description = "Monto del movimiento", example = "1000.00")
    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    @JsonProperty("monto")
    private BigDecimal amount;

    public enum MovementType {
        ENTRADA, SALIDA
    }

    @PrePersist
    protected void onCreate() {
        movementDate = LocalDateTime.now();
    }
}
