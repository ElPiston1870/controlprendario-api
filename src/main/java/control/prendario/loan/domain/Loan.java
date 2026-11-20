package control.prendario.loan.domain;

import control.prendario.client.domain.Client;
import control.prendario.payment.domain.Payment;
import control.prendario.vehicle.domain.Vehicle;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Entidad que representa un préstamo prendario")
@Entity
@Table(name = "prestamos")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Loan {

    @Schema(description = "Identificador único del préstamo", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prestamo")
    @JsonProperty("idPrestamo")
    private Long loanId;

    @Schema(description = "Cliente asociado al préstamo")
    @ManyToOne
    @JoinColumn(name = "id_cliente")
    @JsonProperty("cliente")
    private Client client;

    @Schema(description = "Lista de pagos realizados")
    @OneToMany(mappedBy = "loan")
    @JsonIgnore // Ignora la lista de pagos al serializar
    private List<Payment> payments;

    @Schema(description = "Vehículo en garantía")
    @ManyToOne
    @JoinColumn(name = "id_vehiculo")
    @JsonProperty("vehiculo")
    private Vehicle vehicle;

    @Schema(description = "Monto del préstamo", example = "10000.00")
    @Column(name = "fecha_prestamo")
    @JsonProperty("fechaPrestamo")
    private LocalDateTime loanDate;

    @Schema(description = "Fecha de vencimiento del prestamo")
    @Column(name = "fecha_vencimiento")
    @JsonProperty("fechaVencimiento")
    private LocalDateTime dueDate;

    @Schema(description = "Monto del prestamo")
    @Column(name = "monto_prestamo")
    @JsonProperty("montoPrestamo")
    private BigDecimal loanAmount;

    @Schema(description = "Tasa de interés anual", example = "5.0")
    @Column(name = "tasa_interes")
    @JsonProperty("tasaInteres")
    private BigDecimal interestRate;

    @Schema(description = "Estado actual del préstamo",
            allowableValues = {"ACTIVO", "VENCIDO", "PAGADO"},
            example = "ACTIVO")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_prestamo")
    @JsonProperty("estadoPrestamo")
    private LoanStatus loanStatus = LoanStatus.ACTIVO;

    @Schema(description = "Observaciones del prestamo")
    @Column(name = "observaciones")
    @JsonProperty("observaciones")
    private String notes;

    @Column(name = "interes_total")
    @JsonProperty("interesTotal")
    private BigDecimal totalInterest;

    @Column(name = "capital_pagado")
    @JsonProperty("capitalPagado")
    private BigDecimal paidPrincipal;

    @Column(name = "interes_pagado")
    @JsonProperty("interesPagado")
    private BigDecimal paidInterest;

    @Column(name = "interes_pendiente")
    @JsonProperty("interesPendiente")
    private  BigDecimal pendingInterest;

    @Column(name = "capital_pendiente")
    @JsonProperty("capitalPendiente")
    private  BigDecimal pendingPrincipal;

    @Schema(description = "Indica si el préstamo está activo o archivado", example = "true")
    @Column(name = "activo")
    @JsonProperty("activo")
    private Boolean active = true;

    @PrePersist
    protected void onCreate() {
        loanDate = LocalDateTime.now();
        // Establecer fecha de vencimiento a 3 meses después
        dueDate = loanDate.plusMonths(3);
        // Establecer tasa de interés por defecto si no se ha establecido
        if (interestRate == null) {
            interestRate = new BigDecimal("5.0");
        }
        if (active == null) {
            active = true;
        }
    }

}
