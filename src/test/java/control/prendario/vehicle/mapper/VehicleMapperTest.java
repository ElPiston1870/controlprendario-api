package control.prendario.vehicle.mapper;

import control.prendario.vehicle.domain.Vehicle;
import control.prendario.vehicle.domain.VehicleType;
import control.prendario.vehicle.dto.VehicleDTO;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class VehicleMapperTest {

    private final VehicleMapper mapper = new VehicleMapper();

    @Test
    void convertsFullDtoToEntity() {
        VehicleDTO dto = dto();
        dto.setVehicleId(4L);

        Vehicle vehicle = mapper.toEntity(dto);

        assertEquals(4L, vehicle.getVehicleId());
        assertEquals(VehicleType.CAMIONETA, vehicle.getVehicleType());
        assertEquals(dto.getBrand(), vehicle.getBrand());
        assertEquals(dto.getLine(), vehicle.getLine());
        assertEquals(dto.getModel(), vehicle.getModel());
        assertEquals(dto.getPlate(), vehicle.getPlate());
        assertEquals(dto.getEngineDisplacement(), vehicle.getEngineDisplacement());
        assertEquals(dto.getColor(), vehicle.getColor());
        assertEquals(dto.getEngineNumber(), vehicle.getEngineNumber());
        assertEquals(dto.getChassisNumber(), vehicle.getChassisNumber());
        assertEquals(dto.getRegistrationPlace(), vehicle.getRegistrationPlace());
        assertEquals(dto.getRegistrationDate(), vehicle.getRegistrationDate());
        assertEquals(dto.getOwner(), vehicle.getOwner());
        assertEquals(dto.getOwnerDocumentNumber(), vehicle.getOwnerDocumentNumber());
    }

    @Test
    void convertsEntityToDtoAllowingNullId() {
        Vehicle vehicle = mapper.toEntity(dto());
        VehicleDTO dto = mapper.toDTO(vehicle);

        assertNull(vehicle.getVehicleId());
        assertEquals("CAMIONETA", dto.getVehicleType());
        assertEquals(vehicle.getPlate(), dto.getPlate());
        assertEquals(vehicle.getRegistrationDate(), dto.getRegistrationDate());
        assertNull(mapper.toEntity(null));
        assertNull(mapper.toDTO(null));
    }

    private VehicleDTO dto() {
        VehicleDTO dto = new VehicleDTO();
        dto.setVehicleType("CAMIONETA");
        dto.setBrand("Toyota");
        dto.setLine("Hilux");
        dto.setModel(2022);
        dto.setPlate("ABC123");
        dto.setEngineDisplacement(2400);
        dto.setColor("Blanco");
        dto.setEngineNumber("MOTOR");
        dto.setChassisNumber("CHASIS");
        dto.setRegistrationPlace("Bogota");
        dto.setRegistrationDate(new Date(0));
        dto.setOwner("Ana");
        dto.setOwnerDocumentNumber("123");
        return dto;
    }
}
