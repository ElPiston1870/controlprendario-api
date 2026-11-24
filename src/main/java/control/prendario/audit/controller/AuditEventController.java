package control.prendario.audit.controller;

import control.prendario.shared.query.BrowseQuery;
import control.prendario.shared.query.PageResult;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.ModelAttribute;

import control.prendario.audit.domain.AuditEvent;
import control.prendario.audit.service.AuditEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
@Tag(name = "Auditoría", description = "Consulta paginada del registro de acciones")
@SecurityRequirement(name = "JWT")
public class AuditEventController {

    private final AuditEventService auditEventService;

    @GetMapping
    @Operation(summary = "Consultar auditoría por página (solo administradores)")
    public ResponseEntity<PageResult<AuditEvent>> getEvents(@ParameterObject @ModelAttribute BrowseQuery query) {
        return ResponseEntity.ok(auditEventService.pageEvents(query));
    }
}
