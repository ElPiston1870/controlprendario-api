package control.prendario.collateral.controller;

import control.prendario.collateral.dto.CollateralInventoryDTO;
import control.prendario.collateral.service.CollateralInventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import control.prendario.shared.query.BrowseQuery;
import control.prendario.shared.query.PageResult;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.ModelAttribute;

@RestController
@RequestMapping("/api/garantias")
@RequiredArgsConstructor
@Tag(name = "Garantías", description = "Consulta del inventario de garantías prendarias")
@SecurityRequirement(name = "JWT")
public class CollateralController {

    private final CollateralInventoryService collateralInventoryService;

    @GetMapping("/inventario")
    @Operation(summary = "Consultar inventario de garantías", description = "Lista garantías vehiculares y de maquinaria con el estado del préstamo y si continúan en garantía.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página del inventario"),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para consultar garantías")
    })
    public ResponseEntity<PageResult<CollateralInventoryDTO>> getInventory(@ParameterObject @ModelAttribute BrowseQuery query) {
        return ResponseEntity.ok(collateralInventoryService.getInventory(query));
    }
}
