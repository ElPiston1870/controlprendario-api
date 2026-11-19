package control.prendario.service;

import control.prendario.model.Cliente;
import control.prendario.model.EstadoPrestamo;
import control.prendario.model.Prestamo;
import control.prendario.model.PrestamoMaquina;
import control.prendario.model.Vehicle;
import control.prendario.repository.PrestamoMaquinaRepository;
import control.prendario.repository.PrestamoRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GarantiaInventarioServiceTest {

    @Test
    void listaGarantiasVehicularesYDeMaquinariaConEstadoDeOcupacion() {
        PrestamoRepository prestamoRepository = mock(PrestamoRepository.class);
        PrestamoMaquinaRepository prestamoMaquinaRepository = mock(PrestamoMaquinaRepository.class);
        GarantiaInventarioService service =
                new GarantiaInventarioService(prestamoRepository, prestamoMaquinaRepository);

        Cliente cliente = new Cliente();
        cliente.setIdCliente(4L);
        cliente.setNombres("Ana");
        cliente.setApellidos("Pérez");

        Vehicle vehicle = new Vehicle();
        vehicle.setMarca("Toyota");
        vehicle.setLinea("Corolla");
        vehicle.setPlaca("ABC123");

        Prestamo vehicular = new Prestamo();
        vehicular.setIdPrestamo(2L);
        vehicular.setCliente(cliente);
        vehicular.setVehiculo(vehicle);
        vehicular.setMontoPrestamo(new BigDecimal("1000"));
        vehicular.setEstadoPrestamo(EstadoPrestamo.ACTIVO);

        PrestamoMaquina maquinaria = new PrestamoMaquina();
        maquinaria.setIdPrestamoMaquina(3L);
        maquinaria.setCliente(cliente);
        maquinaria.setTipoMaquina("Tractor");
        maquinaria.setMarcaMaquina("John Deere");
        maquinaria.setEstadoPrestamo(EstadoPrestamo.PAGADO);

        when(prestamoRepository.findAll()).thenReturn(List.of(vehicular));
        when(prestamoMaquinaRepository.findAll()).thenReturn(List.of(maquinaria));

        var inventory = service.consultarInventario();

        assertEquals(2, inventory.size());
        assertEquals("VEHICULO", inventory.get(0).getTipoGarantia());
        assertTrue(inventory.get(0).isEnGarantia());
        assertEquals("MAQUINARIA", inventory.get(1).getTipoGarantia());
        assertTrue(!inventory.get(1).isEnGarantia());
    }
}
