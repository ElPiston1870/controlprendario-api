package control.prendario.vehicle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock private VehicleMapper mapper;
    @Mock private VehicleRepository repository;
    @InjectMocks private VehicleService service;

    @Test
    void createsVehicleWhenPlateDoesNotExist() {
        VehicleDTO dto = dto("ABC123");
        Vehicle vehicle = new Vehicle();
        when(repository.findByPlate("ABC123")).thenReturn(Optional.empty());
        when(mapper.toEntity(dto)).thenReturn(vehicle);
        when(repository.save(vehicle)).thenReturn(vehicle);

        assertSame(vehicle, service.saveVehicle(dto));
        assertTrue(vehicle.getActive());
    }

    @Test
    void updatesExistingVehicleWithSamePlate() {
        Vehicle existing = vehicle(1L, "ABC123");
        when(repository.findByPlate("ABC123")).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        assertSame(existing, service.saveVehicle(dto("ABC123")));
        assertEquals("NUEVO", existing.getBrand());
        assertTrue(existing.getActive());
        verify(mapper, never()).toEntity(any());
    }

    @Test
    void findsAndArchivesVehiclesWithoutDeletingRecords() {
        Vehicle active = vehicle(1L, "ABC123");
        Vehicle archived = vehicle(2L, "DEF456");
        archived.setActive(false);
        when(repository.findAll()).thenReturn(List.of(active, archived));
        when(repository.findById(2L)).thenReturn(Optional.of(archived));

        assertEquals(List.of(active), service.getAllVehicles());
        assertSame(archived, service.getVehicleById(2L));
        service.deleteVehicle(2L);
        assertFalse(archived.getActive());
        verify(repository).save(archived);
    }

    @Test
    void updatesFieldsAndNormalizesText() {
        Vehicle existing = vehicle(1L, "ABC123");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        VehicleDTO dto = dto("abc123");
        dto.setVehicleId(1L);
        dto.setBrand("honda");
        dto.setVehicleType("MOTOCICLETA");

        assertSame(existing, service.updateVehicle(dto));
        assertEquals("HONDA", existing.getBrand());
        assertEquals(VehicleType.MOTOCICLETA, existing.getVehicleType());
        assertEquals("ABC123", existing.getPlate());
    }

    @Test
    void rejectsUpdatesWithoutIdUnknownVehicleOrDuplicatePlate() {
        assertThrows(RuntimeException.class, () -> service.updateVehicle(dto("ABC123")));

        VehicleDTO dto = dto("ABC123");
        dto.setVehicleId(1L);
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.updateVehicle(dto));

        Vehicle existing = vehicle(1L, "ABC123");
        Vehicle other = vehicle(2L, "DEF456");
        dto.setPlate("DEF456");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.findByPlate("DEF456")).thenReturn(Optional.of(other));
        assertThrows(RuntimeException.class, () -> service.updateVehicle(dto));
    }

    @Test
    void catchesInvalidVehicleTypeDuringUpdate() {
        Vehicle existing = vehicle(1L, "ABC123");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        VehicleDTO dto = dto("ABC123");
        dto.setVehicleId(1L);
        dto.setVehicleType("INVALIDO");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.updateVehicle(dto));
        assertTrue(exception.getMessage().contains("Error al actualizar vehículo"));
    }

    @Test
    void checksExistenceAndSearchesByPlate() {
        when(repository.existsByPlate("ABC123")).thenReturn(true);
        Vehicle vehicle = vehicle(1L, "ABC123");
        when(repository.findByPlate("ABC123")).thenReturn(Optional.of(vehicle));
        assertTrue(service.existsByPlate("ABC123"));
        assertEquals(Optional.of(vehicle), service.findByPlate("ABC123"));
    }

    private Vehicle vehicle(Long id, String plate) {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleId(id);
        vehicle.setPlate(plate);
        vehicle.setVehicleType(VehicleType.AUTOMOVIL);
        vehicle.setBrand("Toyota");
        vehicle.setLine("Corolla");
        return vehicle;
    }

    private VehicleDTO dto(String plate) {
        VehicleDTO dto = new VehicleDTO();
        dto.setPlate(plate);
        dto.setVehicleType("AUTOMOVIL");
        dto.setBrand("Nuevo");
        dto.setLine("Linea");
        dto.setModel(2022);
        dto.setEngineDisplacement(1600);
        dto.setColor("Rojo");
        dto.setEngineNumber("motor");
        dto.setChassisNumber("chasis");
        dto.setRegistrationPlace("Bogota");
        dto.setOwner("Ana");
        dto.setOwnerDocumentNumber("123");
        return dto;
    }
}
