package control.prendario.loan.domain;

import control.prendario.client.domain.Client;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Entidad que representa un préstamo de máquina")
@Entity
@Table(name = "prestamos_maquinas")
@Data
public class MachineryLoan {
    @Schema(description = "Identificador único del préstamo", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prestamo_maquina")
    @JsonProperty("idPrestamoMaquina")
    private Long machineryLoanId;

    @Schema(description = "Cliente asociado al préstamo")
    @ManyToOne
    @JoinColumn(name = "id_cliente")
    @JsonProperty("cliente")
    private Client client;

    @Schema(description = "Tipo de maquina")
    @Column(name = "tipo_maquina")
    @JsonProperty("tipoMaquina")
    private String machineType;

    @Schema(description = "Maquina en garantía")
    @Column(name = "marca")
    @JsonProperty("marcaMaquina")
    private String machineBrand;

    @Schema(description = "Fecha del prestamo", example = "10000.00")
    @Column(name = "fecha_prestamo", updatable = false)
    @JsonProperty("fechaPrestamo")
    private LocalDateTime loanDate;

    @Schema(description = "Fecha de vencimiento del prestamo")
    @Column(name = "fecha_vencimiento")
    @JsonProperty("fechaVencimiento")
    private LocalDate dueDate;

    @Schema(description = "Monto del prestamo")
    @Column(name = "monto_prestamo")
    @JsonProperty("montoPrestamo")
    private BigDecimal loanAmount;

    @Schema(description = "Tasa de interés anual", example = "5.0")
    @Column(name = "tasa_interes")
    @JsonProperty("tasaInteres")
    private BigDecimal interestRate;

    @Schema(description = "Estado actual del préstamo",
            allowableValues = {"ACTIVO", "VENCIDO", "PAGADO", "CANCELADO"},
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
        dueDate = LocalDate.now().plusMonths(3);

        if (interestRate == null) {
            interestRate = new BigDecimal("5.0");
        }
        if (active == null) {
            active = true;
        }
    }

}