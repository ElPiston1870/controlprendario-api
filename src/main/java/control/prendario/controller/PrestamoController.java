package control.prendario.controller;

import control.prendario.DTO.PrestamoDTO;
import control.prendario.DTO.PrestamoMaquinaDTO;
import control.prendario.model.Prestamo;
import control.prendario.model.PrestamoMaquina;
import control.prendario.service.PagoMaquinaService;
import control.prendario.service.PagoService;
import control.prendario.service.PrestamoMaquinaService;
import control.prendario.service.PrestamoService;
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
public class PrestamoController {

    @Autowired
    private PrestamoService prestamoService;

    @Autowired
    private PrestamoMaquinaService prestamoMaquinaService;
    @Autowired
    private PagoService pagoService;
    @Autowired
    private PagoMaquinaService pagoMaquinaService;

    @Operation(summary = "Crear nuevo préstamo",
            description = "Crea un nuevo préstamo prendario con la información proporcionada")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Préstamo creado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Prestamo.class)
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
    public ResponseEntity<?> createPrestamo(
            @Parameter(
                    description = "Datos del préstamo a crear",
                    required = true,
                    schema = @Schema(implementation = PrestamoDTO.class)
            )
            @RequestBody PrestamoDTO prestamoDTO) {
        try {
            Prestamo savedPrestamo = prestamoService.savePrestamo(prestamoDTO);
            pagoService.obtenerResumenPagos(prestamoService.getUltimoIdPrestamo() );
            return ResponseEntity.ok(savedPrestamo);
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
                                    implementation = Prestamo.class
                            )
                    )
            )
    })
    @GetMapping("")
    public ResponseEntity<Object> getAllPrestamosAndMaquinas() {
        try {
            List<PrestamoMaquina> maquinas = prestamoMaquinaService.obtenerTodosPrestamosMaquina();
            List<Prestamo> prestamos = prestamoService.getAllPrestamos();

            Map<String, Object> response = new HashMap<>();
            response.put("prestamos", prestamos);
            response.put("maquinas", maquinas);

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
                            schema = @Schema(implementation = Prestamo.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Préstamo no encontrado"
            )
    })
    @GetMapping("/maquina/{id}")
    public ResponseEntity<?> getPrestamoPorId(
            @Parameter(
                    description = "ID del préstamo",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {
        try {
            return ResponseEntity.ok(prestamoMaquinaService.obtenerPrestamoMaquinaPorId(id));
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
                            schema = @Schema(implementation = Prestamo.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Préstamo no encontrado"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getPrestamoById(
            @Parameter(
                    description = "ID del préstamo",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {
        try {
            return ResponseEntity.ok(prestamoService.getPrestamoById(id));
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
                            schema = @Schema(implementation = Prestamo.class)
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
    public ResponseEntity<?> updatePrestamo(
            @Parameter(
                    description = "ID del préstamo a actualizar",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,
            @Parameter(
                    description = "Nuevos datos del préstamo",
                    required = true,
                    schema = @Schema(implementation = PrestamoDTO.class)
            )
            @RequestBody PrestamoDTO prestamoDTO) {
        try {
            ResponseEntity<Prestamo> respuesta = ResponseEntity.ok(prestamoService.updatePrestamo(id, prestamoDTO));
            pagoService.obtenerResumenPagos(id);
            return respuesta;
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
                            schema = @Schema(implementation = Prestamo.class)
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
    public ResponseEntity<?> actualizarPrestamo(
            @Parameter(
                    description = "ID del préstamo a actualizar",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,
            @Parameter(
                    description = "Nuevos datos del préstamo",
                    required = true,
                    schema = @Schema(implementation = PrestamoDTO.class)
            )
            @RequestBody PrestamoMaquinaDTO prestamoMaquinaDTO) {
        try {
            ResponseEntity<PrestamoMaquina> respuesta = ResponseEntity.ok(prestamoMaquinaService.actualizarPrestamoMaquina(id, prestamoMaquinaDTO));
            pagoMaquinaService.obtenerResumenPagos(id);
            return respuesta;
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
    public ResponseEntity<?> deletePrestamo(
            @Parameter(
                    description = "ID del préstamo a eliminar",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {
        try {
            prestamoService.deletePrestamo(id);
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
    public ResponseEntity<?> deletePrestamoMaquina(
            @Parameter(
                    description = "ID del préstamo a eliminar",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {
        try {
            prestamoMaquinaService.eliminarPrestamoMaquina(id);
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
                                    implementation = Prestamo.class
                            )
                    )
            )
    })
    @GetMapping("/buscar")
    public ResponseEntity<List<Prestamo>> buscarPrestamos(
            @Parameter(description = "Término general de búsqueda")
            @RequestParam(required = false) String termino,

            @Parameter(description = "Nombre del cliente")
            @RequestParam(required = false) String nombreCliente,

            @Parameter(description = "Número de documento del cliente")
            @RequestParam(required = false) String numeroDocumento) {

        if (termino != null) {
            return ResponseEntity.ok(prestamoService.buscarPorTermino(termino));
        }
        return ResponseEntity.ok(prestamoService.buscarPorFiltros(termino, numeroDocumento));
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
                                    implementation = Prestamo.class
                            )
                    )
            )
    })
    @GetMapping("/maquina/buscar")
    public ResponseEntity<List<PrestamoMaquina>> buscarPrestamoMaquina(
            @Parameter(description = "Término general de búsqueda")
            @RequestParam(required = false) String termino,

            @Parameter(description = "Nombre del cliente")
            @RequestParam(required = false) String nombreCliente,

            @Parameter(description = "Número de documento del cliente")
            @RequestParam(required = false) String numeroDocumento) {

        if (termino != null) {
            return ResponseEntity.ok(prestamoMaquinaService.buscarPorTermino(termino));
        }
        return ResponseEntity.ok(prestamoMaquinaService.buscarPorFiltros(termino, numeroDocumento));
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
                                    implementation = Prestamo.class
                            )
                    )
            )
    })
    @GetMapping("/vencidos")
    public ResponseEntity<List<Prestamo>> getPrestamosVencidos() {
        return ResponseEntity.ok(prestamoService.findPrestamosVencidos());
    }

    @GetMapping("/maquinas/vencidos")
    public ResponseEntity<List<PrestamoMaquina>> getMaquinasVencidas() {
        return ResponseEntity.ok(prestamoMaquinaService.findPrestamosVencidos());
    }

    @GetMapping("/ultimo-id")
    public ResponseEntity<Long> obtenerUltimoIdPrestamo() {
        Long ultimoId = prestamoService.getUltimoIdPrestamo();
        System.out.println("Ultimo Id: " + ultimoId);
        return ResponseEntity.ok(ultimoId);
    }

    @GetMapping("/ultimo-id/maquina")
    public ResponseEntity<Long> obtenerUltimoIdPrestamoMaquina() {
        Long ultimoId = prestamoMaquinaService.getUltimoIdPrestamo();
        System.out.println("Ultimo Id: " + ultimoId);
        return ResponseEntity.ok(ultimoId);
    }
}