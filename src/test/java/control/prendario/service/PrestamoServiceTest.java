package control.prendario.service;

import control.prendario.DTO.PrestamoDTO;
import control.prendario.DTO.PrestamoMaquinaDTO;
import control.prendario.DTO.VehicleDTO;
import control.prendario.model.Cliente;
import control.prendario.model.EstadoPrestamo;
import control.prendario.model.Prestamo;
import control.prendario.model.PrestamoMaquina;
import control.prendario.model.TipoVehiculo;
import control.prendario.model.Vehicle;
import control.prendario.repository.PrestamoMaquinaRepository;
import control.prendario.repository.PrestamoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrestamoServiceTest {

    @Mock private PrestamoRepository prestamoRepository;
    @Mock private ClienteService clienteService;
    @Mock private VehicleService vehicleService;
    @Mock private PrestamoMaquinaRepository maquinaRepository;
    @InjectMocks private PrestamoService service;
    @InjectMocks private PrestamoMaquinaService maquinaService;

    @Test
    void creaPrestamoVehicularConVehiculoNuevoYEstadoPorDefecto() {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(1L);
        Vehicle vehicle = new Vehicle();
        vehicle.setIdVehiculo(4L);
        when(clienteService.obtenerActivoPorId(1L)).thenReturn(cliente);
        when(vehicleService.findByPlaca("ABC123")).thenReturn(Optional.empty());
        when(vehicleService.saveVehicle(any(VehicleDTO.class))).thenReturn(vehicle);
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));
        PrestamoDTO dto = prestamoDTO();

        Prestamo prestamo = service.savePrestamo(dto);

        assertSame(cliente, prestamo.getCliente());
        assertSame(vehicle, prestamo.getVehiculo());
        assertEquals(EstadoPrestamo.ACTIVO, prestamo.getEstadoPrestamo());
        verify(vehicleService).saveVehicle(dto.getVehiculo());
    }

    @Test
    void creaPrestamoVehicularActualizandoVehiculoExistenteYEstadoIndicado() {
        Vehicle vehicle = new Vehicle();
        vehicle.setIdVehiculo(7L);
        Cliente cliente = new Cliente();
        when(clienteService.obtenerActivoPorId(1L)).thenReturn(cliente);
        when(vehicleService.findByPlaca("ABC123")).thenReturn(Optional.of(vehicle));
        when(vehicleService.updateVehicle(any(VehicleDTO.class))).thenReturn(vehicle);
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));
        PrestamoDTO dto = prestamoDTO();
        dto.setEstadoPrestamo(EstadoPrestamo.PENDIENTE);

        Prestamo prestamo = service.savePrestamo(dto);

        assertEquals(EstadoPrestamo.PENDIENTE, prestamo.getEstadoPrestamo());
        assertEquals(7L, dto.getVehiculo().getIdVehiculo());
        verify(vehicleService).updateVehicle(dto.getVehiculo());
    }

    @Test
    void actualizaPrestamoVehicularYPermiteCambiarCliente() {
        Prestamo existente = new Prestamo();
        existente.setIdPrestamo(2L);
        Vehicle vehicle = new Vehicle();
        vehicle.setIdVehiculo(3L);
        existente.setVehiculo(vehicle);
        when(prestamoRepository.findById(2L)).thenReturn(Optional.of(existente));
        when(clienteService.obtenerPorId(9L)).thenReturn(new Cliente());
        when(vehicleService.updateVehicle(any(VehicleDTO.class))).thenAnswer(inv -> {
            Vehicle actualizado = new Vehicle();
            actualizado.setIdVehiculo(3L);
            return actualizado;
        });
        when(prestamoRepository.save(existente)).thenReturn(existente);
        PrestamoDTO dto = prestamoDTO();
        dto.setEstadoPrestamo(EstadoPrestamo.VENCIDO);
        dto.setIdCliente(9L);

        assertSame(existente, service.updatePrestamo(2L, dto));
        assertEquals(EstadoPrestamo.VENCIDO, existente.getEstadoPrestamo());
        assertEquals(3L, dto.getVehiculo().getIdVehiculo());
        assertNotNull(existente.getCliente());
    }

    @Test
    void filtraPrestamosArchivadosYConsultaBusquedaVencidosEIdMaximo() {
        Prestamo activo = new Prestamo();
        Prestamo archivado = new Prestamo();
        archivado.setActivo(false);
        when(prestamoRepository.findAllByOrderByIdPrestamoDesc()).thenReturn(List.of(activo, archivado));
        when(prestamoRepository.findByEstadoPrestamo(EstadoPrestamo.VENCIDO)).thenReturn(List.of(activo, archivado));
        when(prestamoRepository.findByBusquedaGeneral("Ana")).thenReturn(List.of(activo, archivado));
        when(prestamoRepository.findByClienteNombresContainingIgnoreCaseOrClienteApellidosContainingIgnoreCase("Ana", "Ana"))
                .thenReturn(List.of(activo, archivado));
        when(prestamoRepository.findByClienteNumeroDocumentoContaining("123")).thenReturn(List.of(activo, archivado));
        when(prestamoRepository.findMaxId()).thenReturn(10L);

        assertEquals(List.of(activo), service.getAllPrestamos());
        assertEquals(List.of(activo), service.findPrestamosVencidos());
        assertEquals(List.of(activo), service.buscarPorTermino("Ana"));
        assertEquals(List.of(activo), service.buscarPorFiltros("Ana", "123"));
        assertEquals(List.of(activo), service.buscarPorFiltros(null, "123"));
        assertEquals(List.of(activo), service.buscarPorFiltros(null, null));
        assertEquals(10L, service.getUltimoIdPrestamo());
    }

    @Test
    void creaYActualizaPrestamoDeMaquinaria() {
        Cliente cliente = new Cliente();
        when(clienteService.obtenerActivoPorId(1L)).thenReturn(cliente);
        when(maquinaRepository.save(any(PrestamoMaquina.class))).thenAnswer(inv -> inv.getArgument(0));
        PrestamoMaquinaDTO dto = new PrestamoMaquinaDTO();
        dto.setIdCliente(1L);
        dto.setTipoMaquina("Tractor");
        dto.setMarcaMaquina("Marca");
        dto.setMontoPrestamo(new BigDecimal("1000"));

        PrestamoMaquina maquina = maquinaService.crearPrestamoMaquina(dto);
        assertSame(cliente, maquina.getCliente());
        assertEquals(new BigDecimal("5.0"), maquina.getTasaInteres());
        assertEquals(EstadoPrestamo.ACTIVO, maquina.getEstadoPrestamo());

        maquina.setIdPrestamoMaquina(3L);
        when(maquinaRepository.findById(3L)).thenReturn(Optional.of(maquina));
        when(clienteService.obtenerPorId(2L)).thenReturn(new Cliente());
        dto.setIdCliente(2L);
        dto.setTasaInteres(new BigDecimal("8"));
        dto.setEstadoPrestamo(EstadoPrestamo.VENCIDO);
        dto.setObservaciones("Actualizado");
        when(maquinaRepository.save(maquina)).thenReturn(maquina);

        assertSame(maquina, maquinaService.actualizarPrestamoMaquina(3L, dto));
        assertEquals(EstadoPrestamo.VENCIDO, maquina.getEstadoPrestamo());
        assertEquals(new BigDecimal("8"), maquina.getTasaInteres());
        assertEquals("Actualizado", maquina.getObservaciones());
        assertNotSame(cliente, maquina.getCliente());
    }

    @Test
    void filtraPrestamosDeMaquinariaYDetectaVencidos() {
        PrestamoMaquina activo = new PrestamoMaquina();
        activo.setEstadoPrestamo(EstadoPrestamo.ACTIVO);
        activo.setFechaVencimiento(LocalDate.now().minusDays(1));
        PrestamoMaquina futuro = new PrestamoMaquina();
        futuro.setEstadoPrestamo(EstadoPrestamo.ACTIVO);
        futuro.setFechaVencimiento(LocalDate.now().plusDays(1));
        PrestamoMaquina archivado = new PrestamoMaquina();
        archivado.setActivo(false);
        when(maquinaRepository.findAll()).thenReturn(List.of(activo, futuro, archivado));
        when(maquinaRepository.findByEstadoPrestamo(EstadoPrestamo.VENCIDO)).thenReturn(List.of(activo, archivado));
        when(maquinaRepository.findAllByOrderByIdPrestamoMaquinaDesc()).thenReturn(List.of(activo, archivado));
        when(maquinaRepository.buscarPorCliente("Ana")).thenReturn(List.of(activo, archivado));
        when(maquinaRepository.findByBusquedaGeneral("Ana")).thenReturn(List.of(activo, archivado));
        when(maquinaRepository.findByClienteNombresContainingIgnoreCaseOrClienteApellidosContainingIgnoreCase("Ana", "Ana"))
                .thenReturn(List.of(activo, archivado));
        when(maquinaRepository.findByClienteNumeroDocumentoContaining("123")).thenReturn(List.of(activo, archivado));
        when(maquinaRepository.findMaxId()).thenReturn(11L);

        assertEquals(List.of(activo), maquinaService.obtenerPrestamosVencidos());
        assertEquals(List.of(activo), maquinaService.findPrestamosVencidos());
        assertEquals(List.of(activo), maquinaService.obtenerTodosPrestamosMaquina());
        assertEquals(List.of(activo), maquinaService.buscarPorCliente("Ana"));
        assertEquals(List.of(activo), maquinaService.buscarPorTermino("Ana"));
        assertEquals(List.of(activo), maquinaService.buscarPorFiltros("Ana", "123"));
        assertEquals(List.of(activo), maquinaService.buscarPorFiltros(null, "123"));
        assertEquals(List.of(activo), maquinaService.buscarPorFiltros(null, null));
        assertEquals(11L, maquinaService.getUltimoIdPrestamo());
    }

    private PrestamoDTO prestamoDTO() {
        PrestamoDTO dto = new PrestamoDTO();
        dto.setIdCliente(1L);
        dto.setMontoPrestamo(new BigDecimal("1000"));
        dto.setTasaInteres(new BigDecimal("5"));
        VehicleDTO vehicle = new VehicleDTO();
        vehicle.setPlaca("ABC123");
        vehicle.setTipoVehiculo("AUTOMOVIL");
        dto.setVehiculo(vehicle);
        return dto;
    }
}
