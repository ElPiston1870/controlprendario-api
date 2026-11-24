package control.prendario.loan.controller;

import control.prendario.shared.query.BrowseQuery;
import control.prendario.shared.query.PageResult;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.ModelAttribute;

import control.prendario.loan.domain.Loan;
import control.prendario.loan.service.LoanService;
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

@RestController
@RequestMapping("/api/public/prestamos")
@Tag(name = "Consulta de préstamos", description = "Rutas de consulta de préstamos")
@SecurityRequirement(name = "JWT")
public class PublicLoanController {

    @Autowired
    private LoanService loanService;

    @Operation(summary = "Buscar préstamos por página (requiere autenticación)")
    @GetMapping("/buscar")
    public ResponseEntity<PageResult<Loan>> searchLoans(@ParameterObject @ModelAttribute BrowseQuery query) {
        return ResponseEntity.ok(loanService.pageLoans(query, false));
    }

    @Operation(summary = "Consultar el detalle de un préstamo por ID")
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