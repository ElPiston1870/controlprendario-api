package control.prendario.service;

import control.prendario.DTO.VehicleDTO;
import control.prendario.mapper.VehicleMapper;
import control.prendario.model.TipoVehiculo;
import control.prendario.model.Vehicle;
import control.prendario.repository.VehicleRepository;
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
    void creaVehiculoCuandoLaPlacaNoExiste() {
        VehicleDTO dto = dto("ABC123");
        Vehicle vehicle = new Vehicle();
        when(repository.findByPlaca("ABC123")).thenReturn(Optional.empty());
        when(mapper.toEntity(dto)).thenReturn(vehicle);
        when(repository.save(vehicle)).thenReturn(vehicle);

        assertSame(vehicle, service.saveVehicle(dto));
        assertTrue(vehicle.getActivo());
    }

    @Test
    void actualizaVehiculoExistenteConLaMismaPlaca() {
        Vehicle existing = vehicle(1L, "ABC123");
        when(repository.findByPlaca("ABC123")).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        assertSame(existing, service.saveVehicle(dto("ABC123")));
        assertEquals("NUEVO", existing.getMarca());
        assertTrue(existing.getActivo());
        verify(mapper, never()).toEntity(any());
    }

    @Test
    void consultaYArchivaVehiculosSinEliminarRegistros() {
        Vehicle active = vehicle(1L, "ABC123");
        Vehicle archived = vehicle(2L, "DEF456");
        archived.setActivo(false);
        when(repository.findAll()).thenReturn(List.of(active, archived));
        when(repository.findById(2L)).thenReturn(Optional.of(archived));

        assertEquals(List.of(active), service.getAllVehicles());
        assertSame(archived, service.getVehicleById(2L));
        service.deleteVehicle(2L);
        assertFalse(archived.getActivo());
        verify(repository).save(archived);
    }

    @Test
    void actualizaCamposYNormalizaTexto() {
        Vehicle existing = vehicle(1L, "ABC123");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        VehicleDTO dto = dto("abc123");
        dto.setIdVehiculo(1L);
        dto.setMarca("honda");
        dto.setTipoVehiculo("MOTOCICLETA");

        assertSame(existing, service.updateVehicle(dto));
        assertEquals("HONDA", existing.getMarca());
        assertEquals(TipoVehiculo.MOTOCICLETA, existing.getTipoVehiculo());
        assertEquals("ABC123", existing.getPlaca());
    }

    @Test
    void rechazaActualizacionesSinIdVehiculoInexistenteOConPlacaDuplicada() {
        assertThrows(RuntimeException.class, () -> service.updateVehicle(dto("ABC123")));

        VehicleDTO dto = dto("ABC123");
        dto.setIdVehiculo(1L);
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.updateVehicle(dto));

        Vehicle existing = vehicle(1L, "ABC123");
        Vehicle other = vehicle(2L, "DEF456");
        dto.setPlaca("DEF456");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.findByPlaca("DEF456")).thenReturn(Optional.of(other));
        assertThrows(RuntimeException.class, () -> service.updateVehicle(dto));
    }

    @Test
    void capturaTipoDeVehiculoInvalidoDuranteLaActualizacion() {
        Vehicle existing = vehicle(1L, "ABC123");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        VehicleDTO dto = dto("ABC123");
        dto.setIdVehiculo(1L);
        dto.setTipoVehiculo("INVALIDO");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.updateVehicle(dto));
        assertTrue(exception.getMessage().contains("Error al actualizar vehículo"));
    }

    @Test
    void consultaExistenciaYBusquedaPorPlaca() {
        when(repository.existsByPlaca("ABC123")).thenReturn(true);
        Vehicle vehicle = vehicle(1L, "ABC123");
        when(repository.findByPlaca("ABC123")).thenReturn(Optional.of(vehicle));
        assertTrue(service.existsByPlaca("ABC123"));
        assertEquals(Optional.of(vehicle), service.findByPlaca("ABC123"));
    }

    private Vehicle vehicle(Long id, String placa) {
        Vehicle vehicle = new Vehicle();
        vehicle.setIdVehiculo(id);
        vehicle.setPlaca(placa);
        vehicle.setTipoVehiculo(TipoVehiculo.AUTOMOVIL);
        vehicle.setMarca("Toyota");
        vehicle.setLinea("Corolla");
        return vehicle;
    }

    private VehicleDTO dto(String placa) {
        VehicleDTO dto = new VehicleDTO();
        dto.setPlaca(placa);
        dto.setTipoVehiculo("AUTOMOVIL");
        dto.setMarca("Nuevo");
        dto.setLinea("Linea");
        dto.setModelo(2022);
        dto.setCilindraje(1600);
        dto.setColor("Rojo");
        dto.setNumeroMotor("motor");
        dto.setNumeroChasis("chasis");
        dto.setSitioMatricula("Bogota");
        dto.setPropietario("Ana");
        dto.setNumeroDocumentoPropietario("123");
        return dto;
    }
}
