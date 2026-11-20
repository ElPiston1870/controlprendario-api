package control.prendario.service;

import control.prendario.model.AuditEvent;
import control.prendario.repository.AuditEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuditEventServiceTest {

    @Test
    void registraEventoConTodosLosDatos() {
        AuditEventRepository repository = mock(AuditEventRepository.class);
        AuditEventService service = new AuditEventService(repository);

        service.registrar("ana", "crear", "ClienteService", 7L, "EXITOSO");

        var captor = org.mockito.ArgumentCaptor.forClass(AuditEvent.class);
        verify(repository).save(captor.capture());
        assertEquals("ana", captor.getValue().getUsuario());
        assertEquals("crear", captor.getValue().getAccion());
        assertEquals("ClienteService", captor.getValue().getRecurso());
        assertEquals(7L, captor.getValue().getRecursoId());
        assertEquals("EXITOSO", captor.getValue().getResultado());
    }

    @Test
    void normalizaPaginaYTamanioDentroDeLimites() {
        AuditEventRepository repository = mock(AuditEventRepository.class);
        Page<AuditEvent> expected = new PageImpl<>(List.of());
        when(repository.findAllByOrderByFechaEventoDesc(any(PageRequest.class))).thenReturn(expected);
        AuditEventService service = new AuditEventService(repository);

        assertEquals(expected, service.consultar(-1, 0));
        verify(repository).findAllByOrderByFechaEventoDesc(PageRequest.of(0, 1));
        service.consultar(3, 500);
        verify(repository).findAllByOrderByFechaEventoDesc(PageRequest.of(3, 100));
    }
}
