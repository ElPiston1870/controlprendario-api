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
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AuditAspectTest {

    private final AuditEventService auditEventService = mock(AuditEventService.class);
    private final AuditAspect aspect = new AuditAspect(auditEventService);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void auditsMutationsAndExtractsIdFromEachEntityType() throws Throwable {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("operador", "secret", java.util.List.of()));
        Object[] entities = {
                entity(new Client(), 1L), entity(new Vehicle(), 2L), entity(new Loan(), 3L),
                entity(new MachineryLoan(), 4L), entity(new Payment(), 5L),
                entity(new MachineryPayment(), 6L), entity(new Movement(), 7L)
        };
        Long[] ids = {1L, 2L, 3L, 4L, 5L, 6L, 7L};

        for (int i = 0; i < entities.length; i++) {
            ProceedingJoinPoint joinPoint = joinPoint("createRecord", new Object[0], entities[i]);
            assertSame(entities[i], aspect.auditMutations(joinPoint));
            verify(auditEventService).register("operador", "createRecord", "Object", ids[i], "EXITOSO");
        }
    }

    @Test
    void skipsAuditForQueriesAndRecordsIdFromArguments() throws Throwable {
        ProceedingJoinPoint query = joinPoint("getEvents", new Object[]{5L}, "lectura");
        assertSame("lectura", aspect.auditMutations(query));
        verifyNoInteractions(auditEventService);

        SecurityContextHolder.clearContext();
        ProceedingJoinPoint mutation = joinPoint("updateRecord", new Object[]{9L}, new Object());
        aspect.auditMutations(mutation);
        verify(auditEventService).register("ANONIMO", "updateRecord", "Object", 9L, "EXITOSO");
    }

    @Test
    void recordsErrorsAndKeepsOriginalExceptionWhenAuditFails() throws Throwable {
        RuntimeException failure = new RuntimeException("fallo");
        ProceedingJoinPoint joinPoint = joinPoint("saveRecord", new Object[0], null);
        when(joinPoint.proceed()).thenThrow(failure);

        assertSame(failure, assertThrows(RuntimeException.class, () -> aspect.auditMutations(joinPoint)));
        verify(auditEventService).register("ANONIMO", "saveRecord", "Object", null, "ERROR");

        RuntimeException auditFailure = new RuntimeException("no se pudo auditar");
        doThrow(auditFailure).when(auditEventService)
                .register("ANONIMO", "deleteRecord", "Object", null, "ERROR");
        ProceedingJoinPoint failingAudit = joinPoint("deleteRecord", new Object[0], null);
        when(failingAudit.proceed()).thenThrow(failure);

        assertSame(failure, assertThrows(RuntimeException.class, () -> aspect.auditMutations(failingAudit)));
        org.junit.jupiter.api.Assertions.assertArrayEquals(new Throwable[]{auditFailure}, failure.getSuppressed());
    }

    private ProceedingJoinPoint joinPoint(String method, Object[] args, Object result) throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(signature.getName()).thenReturn(method);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.getTarget()).thenReturn(new Object());
        when(joinPoint.proceed()).thenReturn(result);
        return joinPoint;
    }

    private Object entity(Object entity, Long id) {
        if (entity instanceof Client value) value.setClientId(id);
        if (entity instanceof Vehicle value) value.setVehicleId(id);
        if (entity instanceof Loan value) value.setLoanId(id);
        if (entity instanceof MachineryLoan value) value.setMachineryLoanId(id);
        if (entity instanceof Payment value) value.setPaymentId(id);
        if (entity instanceof MachineryPayment value) value.setMachineryPaymentId(id);
        if (entity instanceof Movement value) value.setMovementId(id);
        return entity;
    }
}
