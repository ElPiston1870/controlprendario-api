package control.prendario.controller;

import control.prendario.model.Pago;
import control.prendario.model.PagoMaquina;
import control.prendario.service.PagoMaquinaService;
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

        System.out.println( "Pago a insertar de maquinas: " +pago);

        ResponseEntity<PagoMaquina> respuesta = ResponseEntity.ok(pagoMaquinaService.crearPago(pago));
        pagoMaquinaService.obtenerResumenPagos(pago.getPrestamoMaquina().getIdPrestamoMaquina());
        return respuesta;


    }




}