package control.prendario.service;

import control.prendario.model.Cliente;
import control.prendario.model.Movimiento;
import control.prendario.model.Pago;
import control.prendario.model.PagoMaquina;
import control.prendario.model.Prestamo;
import control.prendario.model.PrestamoMaquina;
import control.prendario.model.Vehicle;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Aspect
@Component
public class AuditAspect {

    private static final Set<String> MUTATION_PREFIXES = Set.of(
            "crear", "guardar", "save", "actualizar", "update",
            "eliminar", "delete", "corregir", "aplicarDescuento"
    );

    private final AuditEventService auditEventService;

    public AuditAspect(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @Around("execution(public * control.prendario.service..*(..))")
    public Object auditarMutaciones(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().getName();
        if (MUTATION_PREFIXES.stream().noneMatch(method::startsWith)) {
            return joinPoint.proceed();
        }

        Long resourceId = findResourceId(joinPoint.getArgs());
        String resource = joinPoint.getTarget().getClass().getSimpleName();
        String user = currentUsername();
        try {
            Object result = joinPoint.proceed();
            if (resourceId == null) {
                resourceId = findResourceId(result);
            }
            auditEventService.registrar(user, method, resource, resourceId, "EXITOSO");
            return result;
        } catch (Throwable failure) {
            try {
                auditEventService.registrar(user, method, resource, resourceId, "ERROR");
            } catch (RuntimeException auditFailure) {
                failure.addSuppressed(auditFailure);
            }
            throw failure;
        }
    }

    private Long findResourceId(Object[] arguments) {
        for (Object argument : arguments) {
            if (argument instanceof Long id) {
                return id;
            }
        }
        return null;
    }

    private Long findResourceId(Object result) {
        if (result instanceof Cliente entity) {
            return entity.getIdCliente();
        }
        if (result instanceof Vehicle entity) {
            return entity.getIdVehiculo();
        }
        if (result instanceof Prestamo entity) {
            return entity.getIdPrestamo();
        }
        if (result instanceof PrestamoMaquina entity) {
            return entity.getIdPrestamoMaquina();
        }
        if (result instanceof Pago entity) {
            return entity.getIdPago();
        }
        if (result instanceof PagoMaquina entity) {
            return entity.getIdPagoMaquina();
        }
        if (result instanceof Movimiento entity) {
            return entity.getIdMovimiento();
        }
        return null;
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null || !authentication.isAuthenticated()
                ? "ANONIMO"
                : authentication.getName();
    }
}
