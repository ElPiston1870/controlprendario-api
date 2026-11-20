package control.prendario.service;

import control.prendario.model.EstadoPrestamo;
import control.prendario.model.Movimiento;
import control.prendario.model.Pago;
import control.prendario.repository.MovimientoRepository;
import control.prendario.repository.PagoMaquinaRepository;
import control.prendario.repository.PagoRepository;
import control.prendario.repository.PrestamoMaquinaRepository;
import control.prendario.repository.PrestamoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceTest {

    @Mock private MovimientoRepository movimientoRepository;
    @Mock private PagoRepository pagoRepository;
    @Mock private PagoMaquinaRepository pagoMaquinaRepository;
    @Mock private PrestamoRepository prestamoRepository;
    @Mock private PrestamoMaquinaRepository prestamoMaquinaRepository;
    @InjectMocks private MovimientoService service;

    @Test
    void registraYListaMovimientos() {
        Movimiento movimiento = new Movimiento();
        when(movimientoRepository.save(movimiento)).thenReturn(movimiento);
        when(movimientoRepository.findAllByOrderByFechaMovimientoDesc()).thenReturn(List.of(movimiento));

        assertEquals(movimiento, service.crearMovimiento(movimiento));
        assertEquals(List.of(movimiento), service.obtenerTodosMovimientos());
    }

    @Test
    void cp15CalculaDisponibilidadConEntradasSalidasPrestamosYPagosDeReferencia() {
        when(pagoRepository.sumMontoPagadoByTipoPago(Pago.TipoPago.INTERES)).thenReturn(new BigDecimal("150000"));
        when(pagoMaquinaRepository.sumMontoPagadoByTipoPago(Pago.TipoPago.INTERES)).thenReturn(BigDecimal.ZERO);
        when(movimientoRepository.calculateBalance()).thenReturn(new BigDecimal("1900000"));
        when(pagoRepository.sumTotalPagos()).thenReturn(new BigDecimal("150000"));
        when(pagoMaquinaRepository.sumTotalPagos()).thenReturn(BigDecimal.ZERO);
        when(prestamoRepository.sumTotalPrestamos()).thenReturn(new BigDecimal("1000000"));
        when(prestamoMaquinaRepository.sumTotalPrestamos()).thenReturn(BigDecimal.ZERO);
        when(prestamoRepository.sumarMontosPorEstado(
                EstadoPrestamo.ACTIVO, EstadoPrestamo.VENCIDO, EstadoPrestamo.PENDIENTE))
                .thenReturn(new BigDecimal("1000000"));
        when(prestamoMaquinaRepository.sumarMontosPorEstado(
                EstadoPrestamo.ACTIVO, EstadoPrestamo.VENCIDO, EstadoPrestamo.PENDIENTE))
                .thenReturn(BigDecimal.ZERO);

        Map<String, BigDecimal> balance = service.obtenerBalanceCompleto();

        assertEquals(new BigDecimal("1900000"), balance.get("movimientos"));
        assertEquals(new BigDecimal("150000"), balance.get("pagos"));
        assertEquals(new BigDecimal("150000"), balance.get("interes"));
        assertEquals(new BigDecimal("1000000"), balance.get("prestamos"));
        assertEquals(new BigDecimal("1000000"), balance.get("activos"));
        assertEquals(new BigDecimal("1050000"), balance.get("total"));
    }
}
