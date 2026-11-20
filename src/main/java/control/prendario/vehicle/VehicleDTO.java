package control.prendario.vehicle;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Schema(description = "DTO para la gestión de vehículos")
@Data
@ToString
public class VehicleDTO {
    @Schema(description = "ID del vehículo", example = "1")
    @JsonProperty("idVehiculo")
    private Long vehicleId;

    @Schema(description = "Tipo de vehículo", example = "AUTOMOVIL",
            allowableValues = {"AUTOMOVIL", "MOTOCICLETA", "CAMIONETA"})
    @JsonProperty("tipoVehiculo")
    private String vehicleType;

    @Schema(description = "Marca del vehículo", example = "Toyota")
    @JsonProperty("marca")
    private String brand;

    @Schema(description = "Línea del vehículo", example = "Corolla")
    @JsonProperty("linea")
    private String line;

    @Schema(description = "Modelo (año) del vehículo", example = "2020")
    @JsonProperty("modelo")
    private Integer model;

    @Schema(description = "Placa del vehículo", example = "ABC123")
    @JsonProperty("placa")
    private String plate;

    @Schema(description = "Cilindraje del vehículo", example = "2000")
    @JsonProperty("cilindraje")
    private Integer engineDisplacement;

    @Schema(description = "Color del vehículo", example = "Rojo")
    private String color;

    @Schema(description = "Número de motor", example = "MOT123456")
    @JsonProperty("numeroMotor")
    private String engineNumber;

    @Schema(description = "Número de chasis", example = "CHS789012")
    @JsonProperty("numeroChasis")
    private String chassisNumber;

    @Schema(description = "Sitio de matrícula", example = "Bogotá")
    @JsonProperty("sitioMatricula")
    private String registrationPlace;

    @Schema(description = "Fecha de matrícula", example = "2020-01-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("fechaMatricula")
    private Date registrationDate;

    @Schema(description = "Nombre del propietario", example = "Juan Pérez")
    @JsonProperty("propietario")
    private String owner;

    @Schema(description = "Número de documento del propietario", example = "12345678")
    @JsonProperty("numeroDocumentoPropietario")
    private String ownerDocumentNumber;
}
