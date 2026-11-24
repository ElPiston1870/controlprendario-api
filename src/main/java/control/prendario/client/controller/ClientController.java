package control.prendario.client.controller;

import control.prendario.shared.query.BrowseQuery;
import control.prendario.shared.query.PageResult;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.ModelAttribute;
import control.prendario.client.dto.ClientHistoryPage;
import control.prendario.client.dto.ClientHistoryQuery;

import control.prendario.client.domain.Client;
import control.prendario.client.service.ClientService;
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

    @Operation(summary = "Listar y buscar clientes por página")
    @GetMapping({"", "/buscar"})
    public ResponseEntity<PageResult<Client>> listClients(@ParameterObject @ModelAttribute BrowseQuery query) {
        return ResponseEntity.ok(clientService.pageClients(query));
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

    @Operation(summary = "Consultar historial paginado del cliente")
    @GetMapping("/{id}/historial")
    public ResponseEntity<ClientHistoryPage> getHistory(@PathVariable Long id,
                                                        @ParameterObject @ModelAttribute ClientHistoryQuery query) {
        return ResponseEntity.ok(clientService.getHistory(id, query));
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