package control.prendario.audit.service;

import control.prendario.audit.domain.AuditEvent;
import control.prendario.audit.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditEventService {

    private final AuditEventRepository auditEventRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void register(String username, String action, String resource, Long resourceId, String result) {
        AuditEvent event = new AuditEvent();
        event.setUsername(username);
        event.setAction(action);
        event.setResource(resource);
        event.setResourceId(resourceId);
        event.setResult(result);
        auditEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public Page<AuditEvent> getEvents(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, 100));
        return auditEventRepository.findAllByOrderByEventDateDesc(
                PageRequest.of(safePage, safeSize));
    }
}
