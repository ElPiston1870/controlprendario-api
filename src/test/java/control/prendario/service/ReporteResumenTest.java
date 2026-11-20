package control.prendario.service;

import control.prendario.model.Cliente;
import control.prendario.model.EstadoPrestamo;
import control.prendario.model.Pago;
import control.prendario.model.PagoMaquina;
import control.prendario.model.Prestamo;
import control.prendario.model.PrestamoMaquina;
import control.prendario.model.TipoVehiculo;
import control.prendario.model.Vehicle;
import control.prendario.repository.PagoMaquinaRepository;
import control.prendario.repository.PagoRepository;
import control.prendario.repository.PrestamoMaquinaRepository;
import control.prendario.repository.PrestamoRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReporteResumenTest {

    @Test
    void generaPdfDeResumenInclusoSinPrestamos() throws Exception {
        ReporteService reporteService = new ReporteService(
                mock(PrestamoRepository.class),
                mock(PrestamoMaquinaRepository.class),
                mock(PagoRepository.class),
                mock(PagoMaquinaRepository.class)
        );

        byte[] pdf = reporteService.generarResumenPrestamosPdf();

        assertTrue(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII).startsWith("%PDF-"));
    }

    @Test
    void generaResumenConPrestamosVehicularesYDeMaquinaria() throws Exception {
        PrestamoRepository prestamoRepository = mock(PrestamoRepository.class);
        PrestamoMaquinaRepository maquinaRepository = mock(PrestamoMaquinaRepository.class);
        ReporteService reporteService = new ReporteService(prestamoRepository, maquinaRepository,
                mock(PagoRepository.class), mock(PagoMaquinaRepository.class));
        Cliente cliente = new Cliente();
        cliente.setNombres("Ana");
        cliente.setApellidos("Pérez");
        Vehicle vehicle = new Vehicle();
        vehicle.setMarca("Toyota");
        vehicle.setLinea("Corolla");
        vehicle.setPlaca("ABC123");
        Prestamo vehicular = new Prestamo();
        vehicular.setIdPrestamo(1L);
        vehicular.setCliente(cliente);
        vehicular.setVehiculo(vehicle);
        vehicular.setMontoPrestamo(new BigDecimal("1000"));
        vehicular.setCapitalPagado(new BigDecimal("200"));
        vehicular.setInteresPendiente(new BigDecimal("50"));
        vehicular.setEstadoPrestamo(EstadoPrestamo.ACTIVO);
        Prestamo archivado = new Prestamo();
        archivado.setActivo(false);
        PrestamoMaquina maquinaria = new PrestamoMaquina();
        maquinaria.setIdPrestamoMaquina(2L);
        maquinaria.setTipoMaquina("Tractor");
        maquinaria.setMarcaMaquina("Marca");
        maquinaria.setMontoPrestamo(new BigDecimal("2000"));
        maquinaria.setCapitalPendiente(new BigDecimal("1500"));
        maquinaria.setEstadoPrestamo(EstadoPrestamo.PAGADO);
        when(prestamoRepository.findAll()).thenReturn(List.of(vehicular, archivado));
        when(maquinaRepository.findAll()).thenReturn(List.of(maquinaria));

        byte[] pdf = reporteService.generarResumenPrestamosPdf();

        assertTrue(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII).startsWith("%PDF-"));
    }

    @Test
    void generaRecibosParaPagosVehicularesYMaquinaria() throws Exception {
        PagoRepository pagoRepository = mock(PagoRepository.class);
        PagoMaquinaRepository maquinaPagoRepository = mock(PagoMaquinaRepository.class);
        ReporteService reporteService = new ReporteService(mock(PrestamoRepository.class),
                mock(PrestamoMaquinaRepository.class), pagoRepository, maquinaPagoRepository);
        Cliente cliente = new Cliente();
        cliente.setNombres("Ana");
        cliente.setApellidos("Pérez");
        cliente.setNumeroDocumento("123");
        Vehicle vehicle = new Vehicle();
        vehicle.setMarca("Toyota");
        vehicle.setLinea("Corolla");
        vehicle.setModelo(2022);
        vehicle.setPlaca("ABC123");
        Prestamo prestamo = new Prestamo();
        prestamo.setIdPrestamo(1L);
        prestamo.setCliente(cliente);
        prestamo.setVehiculo(vehicle);
        prestamo.setEstadoPrestamo(EstadoPrestamo.ACTIVO);
        Pago pago = new Pago();
        pago.setIdPago(5L);
        pago.setPrestamo(prestamo);
        pago.setFechaPago(LocalDateTime.now());
        pago.setMontoPagado(new BigDecimal("100"));
        pago.setTipoPago(Pago.TipoPago.CAPITAL);
        pago.setMetodoPago(Pago.MetodoPago.EFECTIVO);
        when(pagoRepository.findById(5L)).thenReturn(Optional.of(pago));

        PrestamoMaquina prestamoMaquina = new PrestamoMaquina();
        prestamoMaquina.setIdPrestamoMaquina(2L);
        prestamoMaquina.setCliente(cliente);
        prestamoMaquina.setTipoMaquina("Tractor");
        prestamoMaquina.setMarcaMaquina("Marca");
        PagoMaquina pagoMaquina = new PagoMaquina();
        pagoMaquina.setIdPagoMaquina(6L);
        pagoMaquina.setPrestamoMaquina(prestamoMaquina);
        pagoMaquina.setFechaPago(LocalDateTime.now());
        pagoMaquina.setMontoPagado(new BigDecimal("200"));
        pagoMaquina.setTipoPago(Pago.TipoPago.INTERES);
        pagoMaquina.setMetodoPago(Pago.MetodoPago.TRANSFERENCIA);
        when(maquinaPagoRepository.findById(6L)).thenReturn(Optional.of(pagoMaquina));

        assertTrue(isPdf(reporteService.generarReciboPago(5L)));
        assertTrue(isPdf(reporteService.generarReciboPagoMaquina(6L)));
    }

    @Test
    void generaContratosVehicularYDeMaquinaria() throws Exception {
        PrestamoRepository prestamoRepository = mock(PrestamoRepository.class);
        PrestamoMaquinaRepository maquinaRepository = mock(PrestamoMaquinaRepository.class);
        ReporteService reporteService = new ReporteService(prestamoRepository, maquinaRepository,
                mock(PagoRepository.class), mock(PagoMaquinaRepository.class));
        Cliente cliente = new Cliente();
        cliente.setNombres("Ana");
        cliente.setApellidos("Pérez");
        cliente.setCiudad("Bogotá");
        cliente.setNumeroDocumento("123");
        Vehicle vehicle = new Vehicle();
        vehicle.setTipoVehiculo(TipoVehiculo.AUTOMOVIL);
        vehicle.setMarca("Toyota");
        vehicle.setLinea("Corolla");
        vehicle.setModelo(2022);
        vehicle.setPlaca("ABC123");
        vehicle.setCilindraje(1600);
        vehicle.setColor("Rojo");
        vehicle.setNumeroMotor("MOTOR");
        vehicle.setNumeroChasis("CHASIS");
        vehicle.setSitioMatricula("Bogotá");
        vehicle.setPropietario("Ana Pérez");
        Prestamo prestamo = new Prestamo();
        prestamo.setCliente(cliente);
        prestamo.setVehiculo(vehicle);
        prestamo.setMontoPrestamo(new BigDecimal("1000"));
        prestamo.setTasaInteres(new BigDecimal("5"));
        prestamo.setFechaPrestamo(LocalDateTime.now());
        prestamo.setFechaVencimiento(LocalDateTime.now().plusMonths(3));
        when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));

        PrestamoMaquina maquina = new PrestamoMaquina();
        maquina.setCliente(cliente);
        maquina.setTipoMaquina("Tractor");
        maquina.setMarcaMaquina("Marca");
        maquina.setMontoPrestamo(new BigDecimal("2000"));
        maquina.setTasaInteres(new BigDecimal("5"));
        maquina.setFechaPrestamo(LocalDateTime.now());
        maquina.setFechaVencimiento(java.time.LocalDate.now().plusMonths(3));
        when(maquinaRepository.findById(2L)).thenReturn(Optional.of(maquina));

        assertTrue(isPdf(reporteService.generarDocumentoEmpeno(1L)));
        assertTrue(isPdf(reporteService.generarDocumentoEmpenoMaquina(2L)));
    }

    private boolean isPdf(byte[] bytes) {
        return new String(bytes, 0, 5, java.nio.charset.StandardCharsets.US_ASCII).startsWith("%PDF-");
    }
}
