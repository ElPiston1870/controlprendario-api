package control.prendario.payment;

import control.prendario.loan.Loan;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Entidad que representa un pago de préstamo")
@Entity
@Table(name = "pagos")
@Data
public class Payment {
    @Schema(description = "Identificador único del pago", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    @JsonProperty("idPago")
    private Long paymentId;

    @Schema(description = "Préstamo asociado al pago")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_prestamo", nullable = false)
    @JsonIdentityReference(alwaysAsId = true)
    @JsonProperty("prestamo")
    private Loan loan;

    @Schema(description = "Fecha y hora del pago")
    @Column(name = "fecha_pago")
    @JsonProperty("fechaPago")
    private LocalDateTime paymentDate;

    @Schema(description = "Monto pagado", example = "500.00")
    @Column(name = "monto_pagado", precision = 12, scale = 2, nullable = false)
    @JsonProperty("montoPagado")
    private BigDecimal amountPaid;

    @Schema(description = "Tipo de pago",
            allowableValues = {"CAPITAL", "INTERES", "MORA"},
            example = "CAPITAL")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pago")
    @JsonProperty("tipoPago")
    private PaymentType paymentType;

    @Schema(description = "Método de pago",
            allowableValues = {"EFECTIVO", "TRANSFERENCIA", "TARJETA"},
            example = "EFECTIVO")
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago")
    @JsonProperty("metodoPago")
    private PaymentMethod paymentMethod;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    @JsonProperty("observaciones")
    private String notes;

    @Schema(description = "Estado del registro de pago", example = "ACTIVO")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago")
    @JsonProperty("estadoPago")
    private PaymentStatus paymentStatus = PaymentStatus.ACTIVO;

    @Schema(description = "Fecha y hora en que se corrigió este pago")
    @Column(name = "fecha_correccion")
    @JsonProperty("fechaCorreccion")
    private LocalDateTime correctionDate;

    @Schema(description = "Motivo de corrección del pago original")
    @Column(name = "motivo_correccion", length = 1000)
    @JsonProperty("motivoCorreccion")
    private String correctionReason;

    @Schema(description = "ID del nuevo pago que reemplaza este registro")
    @Column(name = "corregido_por_pago_id")
    @JsonProperty("corregidoPorPagoId")
    private Long correctedByPaymentId;

    @Schema(description = "ID del pago original reemplazado por este registro")
    @Column(name = "reemplaza_pago_id")
    @JsonProperty("reemplazaPagoId")
    private Long replacesPaymentId;

    @Schema(description = "Indica si el registro es un descuento al interés (no es dinero recibido)", example = "false")
    @Column(name = "es_descuento")
    @JsonProperty("esDescuento")
    private Boolean discount = false;

    public enum PaymentType {
        CAPITAL, INTERES, MORA
    }

    public enum PaymentMethod {
        EFECTIVO, TRANSFERENCIA, TARJETA
    }

    @PrePersist
    protected void onCreate() {
        if (paymentDate == null) {
            paymentDate = LocalDateTime.now();
        }
        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.ACTIVO;
        }
        if (discount == null) {
            discount = false;
        }
    }
}
