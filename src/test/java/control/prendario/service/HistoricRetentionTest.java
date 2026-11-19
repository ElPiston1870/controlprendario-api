package control.prendario.service;

import control.prendario.model.Cliente;
import control.prendario.model.Prestamo;
import control.prendario.model.PrestamoMaquina;
import control.prendario.model.Vehicle;
import control.prendario.repository.ClienteRepository;
import control.prendario.repository.PagoMaquinaRepository;
import control.prendario.repository.PagoRepository;
import control.prendario.repository.PrestamoMaquinaRepository;
import control.prendario.repository.PrestamoRepository;
import control.prendario.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoricRetentionTest {

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private PrestamoRepository prestamoRepository;
    @Mock
    private PrestamoMaquinaRepository prestamoMaquinaRepository;
    @Mock
    private PagoRepository pagoRepository;
    @Mock
    private PagoMaquinaRepository pagoMaquinaRepository;
    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private ClienteService clienteService;
    @InjectMocks
    private PrestamoService prestamoService;
    @InjectMocks
    private PrestamoMaquinaService prestamoMaquinaService;
    @InjectMocks
    private VehicleService vehicleService;

    @Test
    void archivaClienteSinEliminarRegistro() {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(1L);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        clienteService.eliminarCliente(1L);

        assertFalse(cliente.getActivo());
        verify(clienteRepository).save(cliente);
        verify(clienteRepository, never()).deleteById(1L);
    }

    @Test
    void archivaPrestamosYVehiculosSinBorrarlos() {
        Prestamo prestamo = new Prestamo();
        prestamo.setIdPrestamo(2L);
        when(prestamoRepository.findById(2L)).thenReturn(Optional.of(prestamo));

        PrestamoMaquina maquinaria = new PrestamoMaquina();
        maquinaria.setIdPrestamoMaquina(3L);
        when(prestamoMaquinaRepository.findById(3L)).thenReturn(Optional.of(maquinaria));

        Vehicle vehicle = new Vehicle();
        vehicle.setIdVehiculo(4L);
        when(vehicleRepository.findById(4L)).thenReturn(Optional.of(vehicle));

        prestamoService.deletePrestamo(2L);
        prestamoMaquinaService.eliminarPrestamoMaquina(3L);
        vehicleService.deleteVehicle(4L);

        assertFalse(prestamo.getActivo());
        assertFalse(maquinaria.getActivo());
        assertFalse(vehicle.getActivo());
        verify(prestamoRepository).save(prestamo);
        verify(prestamoRepository, never()).deleteById(2L);
        verify(prestamoMaquinaRepository, never()).deleteById(3L);
        verify(vehicleRepository, never()).deleteById(4L);
    }
}
