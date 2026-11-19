package control.prendario.controller;

import control.prendario.model.AuditEvent;
import control.prendario.service.AuditEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
@Tag(name = "Auditoría", description = "Consulta paginada del registro de acciones")
@SecurityRequirement(name = "JWT")
public class AuditEventController {

    private final AuditEventService auditEventService;

    @GetMapping
    @Operation(summary = "Consultar auditoría", description = "Lista las operaciones registradas, ordenadas de más reciente a más antigua. Solo administradores.")
    public ResponseEntity<Page<AuditEvent>> consultar(
            @Parameter(description = "Número de página, desde cero")
            @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Registros por página (máximo 100)")
            @RequestParam(defaultValue = "25") int tamano
    ) {
        return ResponseEntity.ok(auditEventService.consultar(pagina, tamano));
    }
}
