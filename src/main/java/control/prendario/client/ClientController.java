package control.prendario.client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "API para la gestión de clientes del sistema de préstamos prendarios")
@SecurityRequirement(name = "JWT")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Operation(summary = "Crear nuevo cliente",
            description = "Crea un nuevo cliente en el sistema con la información proporcionada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Cliente creado exitosamente",
                    content = @Content(schema = @Schema(implementation = Client.class))),
            @ApiResponse(responseCode = "400",
                    description = "Datos del cliente inválidos"),
            @ApiResponse(responseCode = "409",
                    description = "Ya existe un cliente con el mismo número de documento")
    })
    @PostMapping
    public ResponseEntity<Client> createClient(
            @Parameter(description = "Datos del cliente a crear", required = true)
            @Valid @RequestBody Client client) {
        return ResponseEntity.ok(clientService.saveClient(client));
    }

    @Operation(summary = "Listar todos los clientes",
            description = "Obtiene una lista de todos los clientes registrados en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Lista de clientes obtenida exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = Client.class)))
    })
    @GetMapping
    public ResponseEntity<List<Client>> listClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @Operation(summary = "Buscar clientes",
            description = "Busca clientes por término general, nombre o número de documento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Búsqueda realizada exitosamente",
                    content = @Content(schema = @Schema(type = "array",
                            implementation = Client.class))),
            @ApiResponse(responseCode = "400",
                    description = "Error en los parámetros de búsqueda")
    })
    @GetMapping("/buscar")
    public ResponseEntity<List<Client>> searchClients(
            @Parameter(description = "Término general de búsqueda")
            @RequestParam(name = "termino", required = false) String term,

            @Parameter(description = "Nombre del cliente")
            @RequestParam(name = "nombre", required = false) String nameFilter,

            @Parameter(description = "Número de documento del cliente")
            @RequestParam(name = "numeroDocumento", required = false) String documentNumber) {
        try {
            List<Client> results = new ArrayList<>();

            if (term != null && !term.trim().isEmpty()) {
                Set<Client> uniqueClients = new LinkedHashSet<>();
                uniqueClients.addAll(clientService.searchByName(term));
                uniqueClients.addAll(clientService.searchByDocumentNumber(term));
                results = new ArrayList<>(uniqueClients);
            } else if (nameFilter != null || documentNumber != null) {
                results = clientService.search(
                        nameFilter != null ? nameFilter : "",
                        documentNumber != null ? documentNumber : ""
                );
            }

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Obtener cliente por ID",
            description = "Obtiene los detalles de un cliente específico por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Cliente encontrado",
                    content = @Content(schema = @Schema(implementation = Client.class))),
            @ApiResponse(responseCode = "404",
                    description = "Cliente no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Client> getClient(
            @Parameter(description = "ID del cliente", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(clientService.getById(id));
    }

    @Operation(
            summary = "Consultar historial del cliente",
            description = "Obtiene la información del cliente y su historial consolidado de préstamos vehiculares y de maquinaria."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Historial obtenido exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClientHistoryDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno al consultar el historial"
            )
    })
    @GetMapping("/{id}/historial")
    public ResponseEntity<ClientHistoryDTO> getHistory(
            @Parameter(
                    name = "id",
                    description = "Identificador único del cliente",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(clientService.getHistory(id));
    }

    @Operation(summary = "Actualizar cliente",
            description = "Actualiza la información de un cliente existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Cliente actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = Client.class))),
            @ApiResponse(responseCode = "400",
                    description = "Datos del cliente inválidos"),
            @ApiResponse(responseCode = "404",
                    description = "Cliente no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Client> updateClient(
            @Parameter(description = "ID del cliente a actualizar", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevos datos del cliente", required = true)
            @Valid @RequestBody Client client) {
        return ResponseEntity.ok(clientService.updateClient(id, client));
    }

    @Operation(summary = "Eliminar cliente",
            description = "Elimina un cliente del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Cliente eliminado exitosamente"),
            @ApiResponse(responseCode = "404",
                    description = "Cliente no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(
            @Parameter(description = "ID del cliente a eliminar", required = true)
            @PathVariable Long id) {
        if (clientService.deleteClient(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}