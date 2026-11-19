package control.prendario.model;

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
public class Pago {
    @Schema(description = "Identificador único del pago", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long idPago;

    @Schema(description = "Préstamo asociado al pago")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_prestamo", nullable = false)
    @JsonIdentityReference(alwaysAsId = true)
    private Prestamo prestamo;

    @Schema(description = "Fecha y hora del pago")
    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Schema(description = "Monto pagado", example = "500.00")
    @Column(name = "monto_pagado", precision = 12, scale = 2, nullable = false)
    private BigDecimal montoPagado;

    @Schema(description = "Tipo de pago",
            allowableValues = {"CAPITAL", "INTERES", "MORA"},
            example = "CAPITAL")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pago")
    private TipoPago tipoPago;

    @Schema(description = "Método de pago",
            allowableValues = {"EFECTIVO", "TRANSFERENCIA", "TARJETA"},
            example = "EFECTIVO")
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago")
    private MetodoPago metodoPago;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Schema(description = "Estado del registro de pago", example = "ACTIVO")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago")
    private EstadoPago estadoPago = EstadoPago.ACTIVO;

    @Schema(description = "Fecha y hora en que se corrigió este pago")
    @Column(name = "fecha_correccion")
    private LocalDateTime fechaCorreccion;

    @Schema(description = "Motivo de corrección del pago original")
    @Column(name = "motivo_correccion", length = 1000)
    private String motivoCorreccion;

    @Schema(description = "ID del nuevo pago que reemplaza este registro")
    @Column(name = "corregido_por_pago_id")
    private Long corregidoPorPagoId;

    @Schema(description = "ID del pago original reemplazado por este registro")
    @Column(name = "reemplaza_pago_id")
    private Long reemplazaPagoId;

    @Schema(description = "Indica si el registro es un descuento al interés (no es dinero recibido)", example = "false")
    @Column(name = "es_descuento")
    private Boolean esDescuento = false;

    public enum TipoPago {
        CAPITAL, INTERES, MORA
    }

    public enum MetodoPago {
        EFECTIVO, TRANSFERENCIA, TARJETA
    }

    @PrePersist
    protected void onCreate() {
        if (fechaPago == null) {
            fechaPago = LocalDateTime.now();
        }
        if (estadoPago == null) {
            estadoPago = EstadoPago.ACTIVO;
        }
        if (esDescuento == null) {
            esDescuento = false;
        }
    }
}
