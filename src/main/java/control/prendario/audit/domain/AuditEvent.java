package control.prendario.audit.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("fechaEvento")
    private LocalDateTime eventDate;

    @Column(name = "usuario", nullable = false, length = 100)
    @JsonProperty("usuario")
    private String username;

    @Column(name = "accion", nullable = false, length = 150)
    @JsonProperty("accion")
    private String action;

    @Column(name = "recurso", nullable = false, length = 100)
    @JsonProperty("recurso")
    private String resource;

    @Column(name = "recurso_id")
    @JsonProperty("recursoId")
    private Long resourceId;

    @Column(name = "resultado", nullable = false, length = 20)
    @JsonProperty("resultado")
    private String result;

    @PrePersist
    protected void onCreate() {
        if (eventDate == null) {
            eventDate = LocalDateTime.now();
        }
    }
}
