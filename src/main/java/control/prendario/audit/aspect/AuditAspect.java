package control.prendario.audit.aspect;

import control.prendario.audit.service.AuditEventService;
import control.prendario.client.domain.Client;
import control.prendario.loan.domain.Loan;
import control.prendario.loan.domain.MachineryLoan;
import control.prendario.movement.domain.Movement;
import control.prendario.payment.domain.MachineryPayment;
import control.prendario.payment.domain.Payment;
import control.prendario.vehicle.domain.Vehicle;

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
            "create", "save", "update", "delete", "correct", "applyInterestDiscount"
    );

    private final AuditEventService auditEventService;

    public AuditAspect(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @Around("execution(public * control.prendario..*Service.*(..))")
    public Object auditMutations(ProceedingJoinPoint joinPoint) throws Throwable {
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
            auditEventService.register(user, method, resource, resourceId, "EXITOSO");
            return result;
        } catch (Throwable failure) {
            try {
                auditEventService.register(user, method, resource, resourceId, "ERROR");
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
        if (result instanceof Client entity) {
            return entity.getClientId();
        }
        if (result instanceof Vehicle entity) {
            return entity.getVehicleId();
        }
        if (result instanceof Loan entity) {
            return entity.getLoanId();
        }
        if (result instanceof MachineryLoan entity) {
            return entity.getMachineryLoanId();
        }
        if (result instanceof Payment entity) {
            return entity.getPaymentId();
        }
        if (result instanceof MachineryPayment entity) {
            return entity.getMachineryPaymentId();
        }
        if (result instanceof Movement entity) {
            return entity.getMovementId();
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
