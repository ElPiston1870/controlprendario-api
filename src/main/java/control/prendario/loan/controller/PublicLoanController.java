package control.prendario.loan.controller;

import control.prendario.loan.domain.Loan;
import control.prendario.loan.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/prestamos")
@Tag(name = "Préstamos Públicos",
        description = "API pública para consulta de préstamos sin autenticación")
public class PublicLoanController {

    @Autowired
    private LoanService loanService;

    @Operation(summary = "Buscar préstamos públicamente",
            description = "Permite buscar préstamos sin necesidad de autenticación. " +
                    "Útil para que los clientes puedan consultar sus préstamos " +
                    "usando su número de documento")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Búsqueda realizada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "array",
                                    implementation = Loan.class,
                                    description = "Lista de préstamos que coinciden con los criterios de búsqueda"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parámetros de búsqueda inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "object",
                                    example = "{\"error\": \"Parámetros de búsqueda inválidos\"}"
                            )
                    )
            )
    })
    @GetMapping("/buscar")
    public ResponseEntity<List<Loan>> searchLoans(
            @Parameter(
                    description = "Término general de búsqueda",
                    example = "Juan"
            )
            @RequestParam(name = "termino", required = false) String term,

            @Parameter(
                    description = "Número de documento del cliente",
                    example = "12345678"
            )
            @RequestParam(name = "numeroDocumento", required = false) String documentNumber) {
        return ResponseEntity.ok(loanService.searchByFilters(term, documentNumber));
    }

    @Operation(summary = "Obtener préstamo por ID públicamente",
            description = "Permite consultar los detalles de un préstamo específico " +
                    "sin necesidad de autenticación")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Préstamo encontrado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = Loan.class,
                                    description = "Detalles del préstamo solicitado"
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
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<Loan> getLoanById(
            @Parameter(
                    description = "ID del préstamo a consultar",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {
        try {
            return ResponseEntity.ok(loanService.getLoanById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}