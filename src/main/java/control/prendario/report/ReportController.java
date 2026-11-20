package control.prendario.report;

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
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
    private final ReportService reportService;

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
    public ResponseEntity<byte[]> generatePledgeContract(
            @Parameter(
                    description = "ID del préstamo para generar el documento",
                    required = true,
                    example = "1",
                    schema = @Schema(type = "integer", format = "int64")
            )
            @PathVariable("idPrestamo") Long loanId) {
        try {
            byte[] reportBytes = reportService.generatePledgeContract(loanId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "documento_empeno.pdf");

            return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);

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
    public ResponseEntity<byte[]> generateMachineryPledgeContract(
            @Parameter(
                    description = "ID del préstamo para generar el documento",
                    required = true,
                    example = "1",
                    schema = @Schema(type = "integer", format = "int64")
            )
            @PathVariable("idPrestamoMaquina") Long machineryLoanId) {
        try {
            byte[] reportBytes = reportService.generateMachineryPledgeContract(machineryLoanId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "documento_maquina.pdf");

            return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);

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
    public ResponseEntity<byte[]> generatePaymentReceipt(
            @Parameter(description = "Identificador del pago vehicular", required = true, example = "1")
            @PathVariable("idPago") Long paymentId) {
        try {
            byte[] receipt = reportService.generatePaymentReceipt(paymentId);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"recibo_pago_" + paymentId + ".pdf\"")
                    .body(receipt);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error al generar recibo del pago vehicular {}", paymentId, e);
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
    public ResponseEntity<byte[]> generateMachineryPaymentReceipt(
            @Parameter(description = "Identificador del pago de maquinaria", required = true, example = "1")
            @PathVariable("idPagoMaquina") Long machineryPaymentId) {
        try {
            byte[] receipt = reportService.generateMachineryPaymentReceipt(machineryPaymentId);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"recibo_pago_maquinaria_" + machineryPaymentId + ".pdf\"")
                    .body(receipt);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error al generar recibo del pago de maquinaria {}", machineryPaymentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Exportar resumen de préstamos en PDF",
            description = "Genera un resumen de préstamos vehiculares y de maquinaria activos, con saldos pendientes y totales."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Resumen PDF generado exitosamente",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PDF_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para generar el reporte"),
            @ApiResponse(responseCode = "500", description = "Error interno al generar el resumen")
    })
    @GetMapping("/resumen-prestamos")
    public ResponseEntity<byte[]> generateLoanSummaryPdf() {
        try {
            byte[] report = reportService.generateLoanSummaryPdf();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"resumen_prestamos.pdf\"")
                    .body(report);
        } catch (Exception e) {
            logger.error("Error al generar resumen de préstamos PDF", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}