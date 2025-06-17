package control.prendario.controller;

import control.prendario.model.Pago;
import control.prendario.model.PagoMaquina;
import control.prendario.service.PagoMaquinaService;
import control.prendario.service.PagoService;
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





}