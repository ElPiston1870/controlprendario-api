package control.prendario.report.dto;

import control.prendario.vehicle.domain.Vehicle;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class VehicleReportDTO {
    @JsonProperty("tipoVehiculo")
    private String vehicleType;
    @JsonProperty("marca")
    private String brand;
    @JsonProperty("linea")
    private String line;
    @JsonProperty("modelo")
    private Integer model;
    @JsonProperty("placa")
    private String plate;
    @JsonProperty("cilindraje")
    private Integer engineDisplacement;
    private String color;
    @JsonProperty("numeroMotor")
    private String engineNumber;
    @JsonProperty("numeroChasis")
    private String chassisNumber;
    @JsonProperty("sitioMatricula")
    private String registrationPlace;
    @JsonProperty("fechaMatricula")
    private Date registrationDate;
    @JsonProperty("propietario")
    private String owner;

    public static VehicleReportDTO fromVehicle(Vehicle vehicle) {
        VehicleReportDTO dto = new VehicleReportDTO();
        dto.setVehicleType(vehicle.getVehicleType().toString());
        dto.setBrand(vehicle.getBrand());
        dto.setLine(vehicle.getLine());
        dto.setModel(vehicle.getModel());
        dto.setPlate(vehicle.getPlate());
        dto.setEngineDisplacement(vehicle.getEngineDisplacement());
        dto.setColor(vehicle.getColor());
        dto.setEngineNumber(vehicle.getEngineNumber());
        dto.setChassisNumber(vehicle.getChassisNumber());
        dto.setRegistrationPlace(vehicle.getRegistrationPlace());
        dto.setRegistrationDate(vehicle.getRegistrationDate());
        dto.setOwner(vehicle.getOwner());
        return dto;
    }
}
