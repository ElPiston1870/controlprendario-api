package control.prendario.vehicle.mapper;

import control.prendario.vehicle.domain.Vehicle;
import control.prendario.vehicle.domain.VehicleType;
import control.prendario.vehicle.dto.VehicleDTO;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class VehicleMapper {

    public Vehicle toEntity(VehicleDTO dto) {
        if (dto == null) return null;

        Vehicle vehicle = new Vehicle();
        if (dto.getVehicleId() != null) {
            vehicle.setVehicleId(dto.getVehicleId());
        }

        vehicle.setVehicleType(VehicleType.valueOf(dto.getVehicleType()));
        vehicle.setBrand(dto.getBrand());
        vehicle.setLine(dto.getLine());
        vehicle.setModel(dto.getModel());
        vehicle.setPlate(dto.getPlate());
        vehicle.setEngineDisplacement(dto.getEngineDisplacement());
        vehicle.setColor(dto.getColor());
        vehicle.setEngineNumber(dto.getEngineNumber());
        vehicle.setChassisNumber(dto.getChassisNumber());
        vehicle.setRegistrationPlace(dto.getRegistrationPlace());
        vehicle.setRegistrationDate(dto.getRegistrationDate()); // Ya no necesitamos conversión
        vehicle.setOwner(dto.getOwner());
        vehicle.setOwnerDocumentNumber(dto.getOwnerDocumentNumber());

        return vehicle;
    }

    public VehicleDTO toDTO(Vehicle entity) {
        if (entity == null) return null;

        VehicleDTO dto = new VehicleDTO();
        dto.setVehicleId(entity.getVehicleId());
        dto.setVehicleType(entity.getVehicleType().name());
        dto.setBrand(entity.getBrand());
        dto.setLine(entity.getLine());
        dto.setModel(entity.getModel());
        dto.setPlate(entity.getPlate());
        dto.setEngineDisplacement(entity.getEngineDisplacement());
        dto.setColor(entity.getColor());
        dto.setEngineNumber(entity.getEngineNumber());
        dto.setChassisNumber(entity.getChassisNumber());
        dto.setRegistrationPlace(entity.getRegistrationPlace());
        dto.setRegistrationDate(entity.getRegistrationDate()); // Ya no necesitamos conversión
        dto.setOwner(entity.getOwner());
        dto.setOwnerDocumentNumber(entity.getOwnerDocumentNumber());

        return dto;
    }
}