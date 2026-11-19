package control.prendario.controller;

import control.prendario.DTO.GarantiaInventarioDTO;
import control.prendario.service.GarantiaInventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/garantias")
@RequiredArgsConstructor
@Tag(name = "Garantías", description = "Consulta del inventario de garantías prendarias")
@SecurityRequirement(name = "JWT")
public class GarantiaController {

    private final GarantiaInventarioService garantiaInventarioService;

    @GetMapping("/inventario")
    @Operation(summary = "Consultar inventario de garantías", description = "Lista garantías vehiculares y de maquinaria con el estado del préstamo y si continúan en garantía.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inventario consultado",
                    content = @Content(schema = @Schema(implementation = GarantiaInventarioDTO.class))),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para consultar garantías")
    })
    public ResponseEntity<List<GarantiaInventarioDTO>> consultarInventario() {
        return ResponseEntity.ok(garantiaInventarioService.consultarInventario());
    }
}
