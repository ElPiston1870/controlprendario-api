package control.prendario.vehicle.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.Date;
import lombok.Data;
import lombok.ToString;

@Schema(description = "Entidad que representa un vehículo en garantía")
@Entity
@Table(name = "vehiculos")
@Data
@ToString
public class Vehicle {
    @Schema(description = "Identificador único del vehículo", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehiculo")
    @JsonProperty("idVehiculo")
    private Long vehicleId;

    @Schema(description = "Tipo de vehículo",
            allowableValues = {"AUTOMOVIL", "MOTOCICLETA", "CAMIONETA"},
            example = "AUTOMOVIL")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vehiculo")
    @JsonProperty("tipoVehiculo")
    private VehicleType vehicleType;

    @Schema(description = "Marca del vehículo", example = "Toyota")
    @Column(name = "marca")
    @JsonProperty("marca")
    private String brand;

    @Schema(description = "Lineea del Vehículo")
    @Column(name = "linea")
    @JsonProperty("linea")
    private String line;

    @Schema(description = "Modelo del vehículo")
    @Column(name = "modelo")
    @JsonProperty("modelo")
    private Integer model;

    @Schema(description = "Placa del vehículo", example = "ABC123")
    @Column(name = "placa", unique = true)
    @JsonProperty("placa")
    private String plate;

    @Schema(description = "Cilindraje del vehículo")
    @Column(name = "cilindraje")
    @JsonProperty("cilindraje")
    private Integer engineDisplacement;

    @Schema(description = "Color del vehículo")
    private String color;

    @Schema(description = "Número de motor", example = "MOT123456")
    @Column(name = "numero_motor")
    @JsonProperty("numeroMotor")
    private String engineNumber;

    @Schema(description = "Numero de Chasis")
    @Column(name = "numero_chasis")
    @JsonProperty("numeroChasis")
    private String chassisNumber;

    @Schema(description = "Sitio de matricula")
    @Column(name = "sitio_matricula")
    @JsonProperty("sitioMatricula")
    private String registrationPlace;

    @Schema(description = "Fecha de matricula")
    @Column(name = "fecha_matricula")
    @Temporal(TemporalType.DATE)
    @JsonProperty("fechaMatricula")
    private Date registrationDate;

    @Schema(description = "Nombre del propietario")
    @Column(name = "propietario")
    @JsonProperty("propietario")
    private String owner;

    @Schema(description = "Numero de documento del propietario")
    @Column(name = "numero_documento_propietario")
    @JsonProperty("numeroDocumentoPropietario")
    private String ownerDocumentNumber;

    @Schema(description = "Fecha de registro del vehículo")
    @Column(name = "fecha_registro")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("fechaRegistro")
    private Date createdAt;

    @Schema(description = "Indica si el vehículo está activo o archivado", example = "true")
    @Column(name = "activo")
    @JsonProperty("activo")
    private Boolean active = true;

    @PrePersist
    protected void onCreate() {
        if (active == null) {
            active = true;
        }
    }
}

