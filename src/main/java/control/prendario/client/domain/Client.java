package control.prendario.client.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Entidad que representa un cliente en el sistema")
@Entity
@Table(name = "clientes")
@Data
public class Client {
    @Schema(description = "Identificador único del cliente", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    @JsonProperty("idCliente")
    private Long clientId;

    @Schema(description = "Tipo de documento de identidad",
            allowableValues = {"DNI", "PASAPORTE", "CEDULA"},
            example = "CEDULA")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento")
    @NotNull(message = "El tipo de documento es obligatorio")
    @JsonProperty("tipoDocumento")
    private DocumentType documentType;

    @Schema(description = "Número de documento de identidad", example = "12345678")
    @Column(name = "numero_documento", unique = true)
    @NotBlank(message = "El número de documento es obligatorio")
    @JsonProperty("numeroDocumento")
    private String documentNumber;

    @Schema(description = "Nombres del cliente", example = "Juan Carlos")
    @NotBlank(message = "Los nombres son obligatorios")
    @Column(name = "nombres")
    @JsonProperty("nombres")
    private String firstNames;

    @Schema(description = "Apellidos del cliente", example = "Pérez González")
    @NotBlank(message = "Los apellidos son obligatorios")
    @Column(name = "apellidos")
    @JsonProperty("apellidos")
    private String lastNames;

    @Schema(description = "Fecha de nacimiento", example = "1990-01-01")
    @Column(name = "fecha_nacimiento")
    @JsonProperty("fechaNacimiento")
    private LocalDate birthDate;

    @Schema(description = "Dirección del cliente", example = "Calle 123 #45-67")
    @Column(name = "direccion")
    @JsonProperty("direccion")
    private String address;

    @Schema(description = "Ciudad de residencia", example = "Bogotá")
    @NotBlank(message = "La ciudad es obligatoria")
    @Column(name = "ciudad")
    @JsonProperty("ciudad")
    private String city;

    @Schema(description = "Número de teléfono", example = "3001234567")
    @NotBlank(message = "El teléfono es obligatorio")
    @Column(name = "telefono")
    @JsonProperty("telefono")
    private String phone;

    @Schema(description = "Correo electrónico", example = "juan.perez@email.com")
    @Email(message = "El correo electrónico no es válido")
    private String email;

    @Schema(description = "Fecha de registro en el sistema")
    @Column(name = "fecha_registro")
    @JsonProperty("fechaRegistro")
    private LocalDateTime createdAt;

    @Schema(description = "Indica si el cliente está activo o archivado", example = "true")
    @Column(name = "activo")
    @JsonProperty("activo")
    private Boolean active = true;
}

 enum DocumentType {
    DNI, PASAPORTE, CEDULA
}
