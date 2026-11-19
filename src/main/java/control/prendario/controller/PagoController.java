package control.prendario.controller;

import control.prendario.DTO.CorreccionPagoDTO;
import control.prendario.model.Pago;
import control.prendario.model.PagoMaquina;
import control.prendario.service.PagoMaquinaService;
import control.prendario.service.PagoService;
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
@RequestMapping("/api/pagos")
@Tag(name = "Pagos",
        description = "API para la gestión de pagos de préstamos prendarios")
@SecurityRequirement(name = "JWT")
public class PagoController {

    @Autowired
    private PagoService pagoService;
    @Autowired
    private PagoMaquinaService pagoMaquinaService;

    @Operation(summary = "Obtener todos los pagos",
            description = "Obtiene una lista de todos los pagos registrados en el sistema")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de pagos obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "array",
                                    implementation = Pago.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No tiene permisos para ver los pagos"
            )
    })
    @GetMapping
    public ResponseEntity<List<Pago>> obtenerTodosPagos() {
        return ResponseEntity.ok(pagoService.obtenerTodosPagos());
    }

    @Operation(summary = "Obtener todos los pagos",
            description = "Obtiene una lista de todos los pagos registrados en el sistema")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de pagos obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "array",
                                    implementation = Pago.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No tiene permisos para ver los pagos"
            )
    })
    @GetMapping("/maquina")
    public ResponseEntity<List<PagoMaquina>> obtenerTodosPagosMaquinas() {
        return ResponseEntity.ok(pagoMaquinaService.obtenerTodosPagos());
    }

    @Operation(summary = "Crear nuevo pago",
            description = "Registra un nuevo pago para un préstamo")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pago registrado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Pago.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos del pago inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "object",
                                    example = "{\"error\": \"El monto del pago es requerido\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Préstamo no encontrado"
            )
    })
    @PostMapping
    public ResponseEntity<Pago> crearPago(
            @Parameter(
                    description = "Datos del pago a registrar",
                    required = true,
                    schema = @Schema(implementation = Pago.class)
            )
            @RequestBody Pago pago) {
        ResponseEntity<Pago> respuesta = ResponseEntity.ok(pagoService.crearPago(pago));
        pagoService.obtenerResumenPagos(pago.getPrestamo().getIdPrestamo());
        return respuesta;
    }

    @Operation(
            summary = "Aplicar descuento de intereses",
            description = "Reduce los intereses pendientes de un préstamo vehicular y registra el descuento como trazabilidad."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Descuento aplicado exitosamente",
                    content = @Content(schema = @Schema(implementation = Pago.class))
            ),
            @ApiResponse(responseCode = "400", description = "Monto inválido o descuento superior al interés pendiente"),
            @ApiResponse(responseCode = "404", description = "Préstamo no encontrado")
    })
    @PostMapping("/descuento-interes/{idPrestamo}")
    public ResponseEntity<Pago> aplicarDescuentoInteres(
            @Parameter(description = "ID del préstamo vehicular", required = true, example = "1")
            @PathVariable Long idPrestamo,
            @Parameter(description = "Monto que se descontará del interés pendiente", required = true, example = "50000")
            @RequestParam BigDecimal montoDescuento
    ) {
        return ResponseEntity.ok(pagoService.aplicarDescuentoInteres(idPrestamo, montoDescuento));
    }

    @Operation(
            summary = "Corregir un pago",
            description = "Conserva el pago original como corregido, registra el motivo y crea un nuevo pago vinculado con los datos correctos."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago corregido y reemplazo registrado",
                    content = @Content(schema = @Schema(implementation = Pago.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o el pago ya fue corregido"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado")
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
            return ResponseEntity.ok(pagoService.corregirPago(idPago, correccion));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }





}