package control.prendario.service;

import control.prendario.model.Cliente;
import control.prendario.model.Movimiento;
import control.prendario.model.Pago;
import control.prendario.model.PagoMaquina;
import control.prendario.model.Prestamo;
import control.prendario.model.PrestamoMaquina;
import control.prendario.model.Vehicle;
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
    void auditaLasMutacionesYExtraeIdDeLosDistintosTiposDeEntidad() throws Throwable {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("operador", "secret", java.util.List.of()));
        Object[] entities = {
                entity(new Cliente(), 1L), entity(new Vehicle(), 2L), entity(new Prestamo(), 3L),
                entity(new PrestamoMaquina(), 4L), entity(new Pago(), 5L),
                entity(new PagoMaquina(), 6L), entity(new Movimiento(), 7L)
        };
        Long[] ids = {1L, 2L, 3L, 4L, 5L, 6L, 7L};

        for (int i = 0; i < entities.length; i++) {
            ProceedingJoinPoint joinPoint = joinPoint("crearRegistro", new Object[0], entities[i]);
            assertSame(entities[i], aspect.auditarMutaciones(joinPoint));
            verify(auditEventService).registrar("operador", "crearRegistro", "Object", ids[i], "EXITOSO");
        }
    }

    @Test
    void omiteAuditoriaParaConsultasYRegistraIdRecibidoEnArgumentos() throws Throwable {
        ProceedingJoinPoint query = joinPoint("consultar", new Object[]{5L}, "lectura");
        assertSame("lectura", aspect.auditarMutaciones(query));
        verifyNoInteractions(auditEventService);

        SecurityContextHolder.clearContext();
        ProceedingJoinPoint mutation = joinPoint("actualizarRegistro", new Object[]{9L}, new Object());
        aspect.auditarMutaciones(mutation);
        verify(auditEventService).registrar("ANONIMO", "actualizarRegistro", "Object", 9L, "EXITOSO");
    }

    @Test
    void registraErroresYPreservaLaExcepcionOriginalSiFallaLaAuditoria() throws Throwable {
        RuntimeException failure = new RuntimeException("fallo");
        ProceedingJoinPoint joinPoint = joinPoint("guardarRegistro", new Object[0], null);
        when(joinPoint.proceed()).thenThrow(failure);

        assertSame(failure, assertThrows(RuntimeException.class, () -> aspect.auditarMutaciones(joinPoint)));
        verify(auditEventService).registrar("ANONIMO", "guardarRegistro", "Object", null, "ERROR");

        RuntimeException auditFailure = new RuntimeException("no se pudo auditar");
        doThrow(auditFailure).when(auditEventService)
                .registrar("ANONIMO", "eliminarRegistro", "Object", null, "ERROR");
        ProceedingJoinPoint failingAudit = joinPoint("eliminarRegistro", new Object[0], null);
        when(failingAudit.proceed()).thenThrow(failure);

        assertSame(failure, assertThrows(RuntimeException.class, () -> aspect.auditarMutaciones(failingAudit)));
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
        if (entity instanceof Cliente value) value.setIdCliente(id);
        if (entity instanceof Vehicle value) value.setIdVehiculo(id);
        if (entity instanceof Prestamo value) value.setIdPrestamo(id);
        if (entity instanceof PrestamoMaquina value) value.setIdPrestamoMaquina(id);
        if (entity instanceof Pago value) value.setIdPago(id);
        if (entity instanceof PagoMaquina value) value.setIdPagoMaquina(id);
        if (entity instanceof Movimiento value) value.setIdMovimiento(id);
        return entity;
    }
}
