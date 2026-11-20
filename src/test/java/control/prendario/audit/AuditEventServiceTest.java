package control.prendario.audit;

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
    void recordsEventWithAllData() {
        AuditEventRepository repository = mock(AuditEventRepository.class);
        AuditEventService service = new AuditEventService(repository);

        service.register("ana", "crear", "ClienteService", 7L, "EXITOSO");

        var captor = org.mockito.ArgumentCaptor.forClass(AuditEvent.class);
        verify(repository).save(captor.capture());
        assertEquals("ana", captor.getValue().getUsername());
        assertEquals("crear", captor.getValue().getAction());
        assertEquals("ClienteService", captor.getValue().getResource());
        assertEquals(7L, captor.getValue().getResourceId());
        assertEquals("EXITOSO", captor.getValue().getResult());
    }

    @Test
    void normalizesPageAndSizeWithinLimits() {
        AuditEventRepository repository = mock(AuditEventRepository.class);
        Page<AuditEvent> expected = new PageImpl<>(List.of());
        when(repository.findAllByOrderByEventDateDesc(any(PageRequest.class))).thenReturn(expected);
        AuditEventService service = new AuditEventService(repository);

        assertEquals(expected, service.getEvents(-1, 0));
        verify(repository).findAllByOrderByEventDateDesc(PageRequest.of(0, 1));
        service.getEvents(3, 500);
        verify(repository).findAllByOrderByEventDateDesc(PageRequest.of(3, 100));
    }
}
