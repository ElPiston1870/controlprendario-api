package control.prendario.loan;

import control.prendario.payment.MachineryPaymentService;
import control.prendario.payment.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prestamos")
@Tag(name = "Préstamos",
        description = "API para la gestión de préstamos prendarios")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @Autowired
    private MachineryLoanService machineryLoanService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private MachineryPaymentService machineryPaymentService;

    @Operation(summary = "Crear nuevo préstamo",
            description = "Crea un nuevo préstamo prendario con la información proporcionada")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Préstamo creado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Loan.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error en los datos del préstamo",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "object",
                                    example = "{\"error\": \"Error al crear el préstamo: datos inválidos\"}"
                            )
                    )
            )
    })
    @PostMapping
    public ResponseEntity<?> createLoan(
            @Parameter(
                    description = "Datos del préstamo a crear",
                    required = true,
                    schema = @Schema(implementation = LoanDTO.class)
            )
            @RequestBody LoanDTO loanDTO) {
        try {
            Loan savedLoan = loanService.saveLoan(loanDTO);
            paymentService.computePaymentSummary(loanService.getLastLoanId() );
            return ResponseEntity.ok(savedLoan);
        } catch (Exception e) {
            String errorMessage = "Error al crear el préstamo: " + e.getMessage();
            return ResponseEntity.badRequest().body(errorMessage);
        }
    }

    @Operation(summary = "Obtener todos los préstamos",
            description = "Obtiene una lista de todos los préstamos registrados en el sistema")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de préstamos obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "array",
                                    implementation = Loan.class
                            )
                    )
            )
    })
    @GetMapping("")
    public ResponseEntity<Object> getAllLoansAndMachineryLoans() {
        try {
            List<MachineryLoan> machines = machineryLoanService.getAllMachineryLoans();
            List<Loan> loans = loanService.getAllLoans();

            Map<String, Object> response = new HashMap<>();
            response.put("prestamos", loans);
            response.put("maquinas", machines);

            return ResponseEntity.ok(response);

        }catch (Exception e){
            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener datos :" + e.getMessage());
        }
    }

    @Operation(summary = "Obtener préstamo por ID",
            description = "Obtiene los detalles de un préstamo específico por su ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Préstamo encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Loan.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Préstamo no encontrado"
            )
    })
    @GetMapping("/maquina/{id}")
    public ResponseEntity<?> getMachineryLoanById(
            @Parameter(
                    description = "ID del préstamo",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {
        try {
            return ResponseEntity.ok(machineryLoanService.getMachineryLoanById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error al obtener el préstamo: " + e.getMessage());
        }
    }

    @Operation(summary = "Obtener préstamo por ID",
            description = "Obtiene los detalles de un préstamo específico por su ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Préstamo encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Loan.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Préstamo no encontrado"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getLoanById(
            @Parameter(
                    description = "ID del préstamo",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {
        try {
            return ResponseEntity.ok(loanService.getLoanById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error al obtener el préstamo: " + e.getMessage());
        }
    }

    @Operation(summary = "Actualizar préstamo",
            description = "Actualiza la información de un préstamo existente")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Préstamo actualizado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Loan.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error al actualizar el préstamo"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Préstamo no encontrado"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateLoan(
            @Parameter(
                    description = "ID del préstamo a actualizar",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,
            @Parameter(
                    description = "Nuevos datos del préstamo",
                    required = true,
                    schema = @Schema(implementation = LoanDTO.class)
            )
            @RequestBody LoanDTO loanDTO) {
        try {
            ResponseEntity<Loan> responseBody = ResponseEntity.ok(loanService.updateLoan(id, loanDTO));
            paymentService.computePaymentSummary(id);
            return responseBody;
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error al actualizar el préstamo: " + e.getMessage());
        }
    }

    @Operation(summary = "Actualizar préstamo",
            description = "Actualiza la información de un préstamo existente")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Préstamo actualizado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Loan.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error al actualizar el préstamo"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Préstamo no encontrado"
            )
    })
    @PutMapping("/maquina/{id}")
    public ResponseEntity<?> updateMachineryLoanById(
            @Parameter(
                    description = "ID del préstamo a actualizar",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,
            @Parameter(
                    description = "Nuevos datos del préstamo",
                    required = true,
                    schema = @Schema(implementation = LoanDTO.class)
            )
            @RequestBody MachineryLoanDTO machineryLoanDTO) {
        try {
            ResponseEntity<MachineryLoan> responseBody = ResponseEntity.ok(machineryLoanService.updateMachineryLoan(id, machineryLoanDTO));
            machineryPaymentService.computePaymentSummary(id);
            return responseBody;
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error al actualizar el préstamo: " + e.getMessage());
        }
    }

    @Operation(summary = "Eliminar préstamo",
            description = "Elimina un préstamo del sistema")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Préstamo eliminado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error al eliminar el préstamo"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Préstamo no encontrado"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLoan(
            @Parameter(
                    description = "ID del préstamo a eliminar",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {
        try {
            loanService.deleteLoan(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error al eliminar el préstamo: " + e.getMessage());
        }
    }

    @Operation(summary = "Eliminar préstamo de maquina",
            description = "Elimina un préstamo del sistema de maquinas")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Préstamo eliminado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error al eliminar el préstamo"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Préstamo no encontrado"
            )
    })
    @DeleteMapping("/maquina/{id}")
    public ResponseEntity<?> deleteMachineryLoanById(
            @Parameter(
                    description = "ID del préstamo a eliminar",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {
        try {
            machineryLoanService.deleteMachineryLoan(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error al eliminar el préstamo: " + e.getMessage());
        }
    }

    @Operation(summary = "Buscar préstamos",
            description = "Busca préstamos por diferentes criterios como término general, " +
                    "nombre del cliente o número de documento")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Búsqueda realizada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "array",
                                    implementation = Loan.class
                            )
                    )
            )
    })
    @GetMapping("/buscar")
    public ResponseEntity<List<Loan>> searchLoans(
            @Parameter(description = "Término general de búsqueda")
            @RequestParam(name = "termino", required = false) String term,

            @Parameter(description = "Nombre del cliente")
            @RequestParam(name = "nombreCliente", required = false) String clientName,

            @Parameter(description = "Número de documento del cliente")
            @RequestParam(name = "numeroDocumento", required = false) String documentNumber) {

        if (term != null) {
            return ResponseEntity.ok(loanService.searchByTerm(term));
        }
        return ResponseEntity.ok(loanService.searchByFilters(term, documentNumber));
    }

    @Operation(summary = "Buscar préstamos",
            description = "Busca préstamos por diferentes criterios como término general, " +
                    "nombre del cliente o número de documento")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Búsqueda realizada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "array",
                                    implementation = Loan.class
                            )
                    )
            )
    })
    @GetMapping("/maquina/buscar")
    public ResponseEntity<List<MachineryLoan>> searchMachineryLoans(
            @Parameter(description = "Término general de búsqueda")
            @RequestParam(name = "termino", required = false) String term,

            @Parameter(description = "Nombre del cliente")
            @RequestParam(name = "nombreCliente", required = false) String clientName,

            @Parameter(description = "Número de documento del cliente")
            @RequestParam(name = "numeroDocumento", required = false) String documentNumber) {

        if (term != null) {
            return ResponseEntity.ok(machineryLoanService.searchByTerm(term));
        }
        return ResponseEntity.ok(machineryLoanService.searchByFilters(term, documentNumber));
    }

    @Operation(summary = "Obtener préstamos vencidos",
            description = "Obtiene una lista de todos los préstamos que han superado su fecha de vencimiento")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de préstamos vencidos obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "array",
                                    implementation = Loan.class
                            )
                    )
            )
    })
    @GetMapping("/vencidos")
    public ResponseEntity<List<Loan>> getOverdueLoans() {
        return ResponseEntity.ok(loanService.findOverdueLoans());
    }

    @GetMapping("/maquinas/vencidos")
    public ResponseEntity<List<MachineryLoan>> getOverdueMachineryLoans() {
        return ResponseEntity.ok(machineryLoanService.findOverdueLoans());
    }

    @GetMapping("/ultimo-id")
    public ResponseEntity<Long> getLastLoanId() {
        Long lastId = loanService.getLastLoanId();
        System.out.println("Ultimo Id: " + lastId);
        return ResponseEntity.ok(lastId);
    }

    @GetMapping("/ultimo-id/maquina")
    public ResponseEntity<Long> getLastMachineryLoanId() {
        Long lastId = machineryLoanService.getLastLoanId();
        System.out.println("Ultimo Id: " + lastId);
        return ResponseEntity.ok(lastId);
    }
}