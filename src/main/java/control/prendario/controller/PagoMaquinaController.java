package control.prendario.controller;

import control.prendario.DTO.CorreccionPagoDTO;
import control.prendario.model.Pago;
import control.prendario.model.PagoMaquina;
import control.prendario.service.PagoMaquinaService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pagos-maquinas")
@Tag(name = "Pagos de Máquinas",
        description = "API para la gestión de pagos de préstamos de máquinas")
@SecurityRequirement(name = "JWT")
public class PagoMaquinaController {

    @Autowired
    private PagoMaquinaService pagoMaquinaService;

    @Operation(summary = "Crear nuevo pago",
            description = "Registra un nuevo pago para un préstamo de máquina")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pago registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = PagoMaquina.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos del pago inválidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Préstamo no encontrado"
            )
    })
    @PostMapping
    public ResponseEntity<PagoMaquina> crearPago(
            @Parameter(description = "Datos del pago a registrar",
                    required = true,
                    schema = @Schema(implementation = PagoMaquina.class))
            @RequestBody PagoMaquina pago) {


        ResponseEntity<PagoMaquina> respuesta = ResponseEntity.ok(pagoMaquinaService.crearPago(pago));
        pagoMaquinaService.obtenerResumenPagos(pago.getPrestamoMaquina().getIdPrestamoMaquina());
        return respuesta;


    }

    @Operation(
            summary = "Aplicar descuento de intereses",
            description = "Reduce los intereses pendientes de un préstamo de maquinaria y registra el descuento como trazabilidad."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Descuento aplicado exitosamente",
                    content = @Content(schema = @Schema(implementation = PagoMaquina.class))
            ),
            @ApiResponse(responseCode = "400", description = "Monto inválido o descuento superior al interés pendiente"),
            @ApiResponse(responseCode = "404", description = "Préstamo de máquina no encontrado")
    })
    @PostMapping("/descuento-interes/{idPrestamoMaquina}")
    public ResponseEntity<PagoMaquina> aplicarDescuentoInteres(
            @Parameter(description = "ID del préstamo de maquinaria", required = true, example = "1")
            @PathVariable Long idPrestamoMaquina,
            @Parameter(description = "Monto que se descontará del interés pendiente", required = true, example = "50000")
            @RequestParam BigDecimal montoDescuento
    ) {
        return ResponseEntity.ok(pagoMaquinaService.aplicarDescuentoInteres(idPrestamoMaquina, montoDescuento));
    }

    @Operation(
            summary = "Corregir un pago de maquinaria",
            description = "Conserva el pago original como corregido, registra el motivo y crea un nuevo pago vinculado con los datos correctos."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago corregido y reemplazo registrado",
                    content = @Content(schema = @Schema(implementation = PagoMaquina.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o el pago ya fue corregido"),
            @ApiResponse(responseCode = "404", description = "Pago de maquinaria no encontrado")
    })
    @PutMapping("/{idPago}/correccion")
    public ResponseEntity<?> corregirPago(
            @Parameter(description = "Identificador del pago a corregir", required = true, example = "1")
            @PathVariable Long idPago,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos correctos del pago y motivo de la corrección",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CorreccionPagoDTO.class))
            )
            @Valid @RequestBody CorreccionPagoDTO correccion
    ) {
        try {
            return ResponseEntity.ok(pagoMaquinaService.corregirPago(idPago, correccion));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



}