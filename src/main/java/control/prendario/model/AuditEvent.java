package control.prendario.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
@Data
@Schema(description = "Registro inmutable de una operación relevante")
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_evento", nullable = false)
    private LocalDateTime fechaEvento;

    @Column(name = "usuario", nullable = false, length = 100)
    private String usuario;

    @Column(name = "accion", nullable = false, length = 150)
    private String accion;

    @Column(name = "recurso", nullable = false, length = 100)
    private String recurso;

    @Column(name = "recurso_id")
    private Long recursoId;

    @Column(name = "resultado", nullable = false, length = 20)
    private String resultado;

    @PrePersist
    protected void onCreate() {
        if (fechaEvento == null) {
            fechaEvento = LocalDateTime.now();
        }
    }
}
