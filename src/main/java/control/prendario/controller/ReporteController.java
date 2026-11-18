package control.prendario.controller;

import control.prendario.service.ReporteService;
import jakarta.persistence.EntityNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes",
        description = "API para la generación de reportes y documentos del sistema")
@SecurityRequirement(name = "JWT")
public class ReporteController {

    private static final Logger logger = LoggerFactory.getLogger(ReporteController.class);
    private final ReporteService reporteService;

    @Operation(summary = "Generar documento de empeño",
            description = "Genera un documento PDF con los detalles del empeño para un préstamo específico. " +
                    "Este documento incluye información del cliente, vehículo y condiciones del préstamo.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Documento generado exitosamente",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PDF_VALUE,
                            schema = @Schema(
                                    type = "string",
                                    format = "binary",
                                    description = "Archivo PDF del documento de empeño"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Préstamo no encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "object",
                                    example = "{\"error\": \"Préstamo no encontrado\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno al generar el documento",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "object",
                                    example = "{\"error\": \"Error al generar el documento de empeño\"}"
                            )
                    )
            )
    })
    @GetMapping("/documento-empeno/{idPrestamo}")
    public ResponseEntity<byte[]> generarDocumentoEmpeno(
            @Parameter(
                    description = "ID del préstamo para generar el documento",
                    required = true,
                    example = "1",
                    schema = @Schema(type = "integer", format = "int64")
            )
            @PathVariable Long idPrestamo) {
        try {
            byte[] reporteBytes = reporteService.generarDocumentoEmpeno(idPrestamo);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "documento_empeno.pdf");

            return new ResponseEntity<>(reporteBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Generar documento de empeño",
            description = "Genera un documento PDF con los detalles del empeño para un préstamo específico. " +
                    "Este documento incluye información del cliente, maquina y condiciones del préstamo.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Documento generado exitosamente",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PDF_VALUE,
                            schema = @Schema(
                                    type = "string",
                                    format = "binary",
                                    description = "Archivo PDF del documento de empeño"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Préstamo no encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "object",
                                    example = "{\"error\": \"Préstamo no encontrado\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno al generar el documento",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "object",
                                    example = "{\"error\": \"Error al generar el documento de empeño\"}"
                            )
                    )
            )
    })
    @GetMapping("/documento-maquina/{idPrestamoMaquina}")
    public ResponseEntity<byte[]> generarDocumentoEmpenoMaquina(
            @Parameter(
                    description = "ID del préstamo para generar el documento",
                    required = true,
                    example = "1",
                    schema = @Schema(type = "integer", format = "int64")
            )
            @PathVariable Long idPrestamoMaquina) {
        try {
            byte[] reporteBytes = reporteService.generarDocumentoEmpenoMaquina(idPrestamoMaquina);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "documento_maquina.pdf");

            return new ResponseEntity<>(reporteBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
            summary = "Generar recibo de pago vehicular",
            description = "Genera un comprobante PDF con los datos registrados del pago, cliente y préstamo vehicular."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Recibo generado exitosamente",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PDF_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno al generar el recibo")
    })
    @GetMapping("/recibo-pago/{idPago}")
    public ResponseEntity<byte[]> generarReciboPago(
            @Parameter(description = "Identificador del pago vehicular", required = true, example = "1")
            @PathVariable Long idPago) {
        try {
            byte[] recibo = reporteService.generarReciboPago(idPago);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"recibo_pago_" + idPago + ".pdf\"")
                    .body(recibo);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error al generar recibo del pago vehicular {}", idPago, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Generar recibo de pago de maquinaria",
            description = "Genera un comprobante PDF con los datos registrados del pago, cliente y préstamo de maquinaria."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Recibo generado exitosamente",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PDF_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Pago de maquinaria no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno al generar el recibo")
    })
    @GetMapping("/recibo-pago-maquina/{idPagoMaquina}")
    public ResponseEntity<byte[]> generarReciboPagoMaquina(
            @Parameter(description = "Identificador del pago de maquinaria", required = true, example = "1")
            @PathVariable Long idPagoMaquina) {
        try {
            byte[] recibo = reporteService.generarReciboPagoMaquina(idPagoMaquina);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"recibo_pago_maquinaria_" + idPagoMaquina + ".pdf\"")
                    .body(recibo);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error al generar recibo del pago de maquinaria {}", idPagoMaquina, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}