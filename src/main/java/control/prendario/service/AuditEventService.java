package control.prendario.service;

import control.prendario.model.AuditEvent;
import control.prendario.repository.AuditEventRepository;
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
    public void registrar(String usuario, String accion, String recurso, Long recursoId, String resultado) {
        AuditEvent event = new AuditEvent();
        event.setUsuario(usuario);
        event.setAccion(accion);
        event.setRecurso(recurso);
        event.setRecursoId(recursoId);
        event.setResultado(resultado);
        auditEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public Page<AuditEvent> consultar(int pagina, int tamano) {
        int paginaSegura = Math.max(0, pagina);
        int tamanoSeguro = Math.max(1, Math.min(tamano, 100));
        return auditEventRepository.findAllByOrderByFechaEventoDesc(
                PageRequest.of(paginaSegura, tamanoSeguro));
    }
}
