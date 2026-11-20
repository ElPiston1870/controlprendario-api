package control.prendario.service;

import control.prendario.model.EstadoPago;
import control.prendario.model.EstadoPrestamo;
import control.prendario.model.Pago;
import control.prendario.model.PagoMaquina;
import control.prendario.model.PrestamoMaquina;
import control.prendario.repository.PagoMaquinaRepository;
import control.prendario.repository.PrestamoMaquinaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoMaquinaServiceTest {

    @Mock private PagoMaquinaRepository pagoRepository;
    @Mock private PrestamoMaquinaRepository prestamoRepository;
    @Mock private CalculoInteresMaquinaService calculoService;
    @InjectMocks private PagoMaquinaService service;

    @Test
    void creaPagoValidoYMarcaPrestamoPagadoAlCubrirCapitalEInteres() {
        PrestamoMaquina prestamo = prestamo(1L, "1000", EstadoPrestamo.ACTIVO);
        PagoMaquina pago = pago(prestamo, "1000", Pago.TipoPago.CAPITAL);
        pago.setEstadoPago(EstadoPago.CORREGIDO);
        pago.setEsDescuento(true);
        pago.setMotivoCorreccion("previo");
        when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));
        when(pagoRepository.sumMontoPagadoByPrestamoAndTipoPago(1L, Pago.TipoPago.CAPITAL))
                .thenReturn(BigDecimal.ZERO);
        when(pagoRepository.sumMontoPagadoByPrestamoAndTipoPago(1L, Pago.TipoPago.INTERES))
                .thenReturn(new BigDecimal("150"));
        when(pagoRepository.save(pago)).thenReturn(pago);

        assertSame(pago, service.crearPago(pago));
        assertEquals(EstadoPago.ACTIVO, pago.getEstadoPago());
        assertFalse(pago.getEsDescuento());
        assertNull(pago.getMotivoCorreccion());
        assertEquals(EstadoPrestamo.PAGADO, prestamo.getEstadoPrestamo());
        verify(prestamoRepository).save(prestamo);
    }

    @Test
    void rechazaPagoDePrestamoInexistenteYValidaNulos() {
        PagoMaquina sinPrestamo = new PagoMaquina();
        assertThrows(IllegalArgumentException.class, () -> service.crearPago(null));
        assertThrows(IllegalArgumentException.class, () -> service.crearPago(sinPrestamo));

        PagoMaquina pago = pago(prestamo(2L, "1000", EstadoPrestamo.ACTIVO), "100", Pago.TipoPago.CAPITAL);
        when(prestamoRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> service.crearPago(pago));
    }

    @Test
    void creaDescuentoSoloDentroDelInteresPendiente() {
        PrestamoMaquina prestamo = prestamo(3L, "1000", EstadoPrestamo.ACTIVO);
        when(prestamoRepository.findById(3L)).thenReturn(Optional.of(prestamo));
        when(calculoService.calcularInteresPendiente(eq(prestamo), any())).thenReturn(new BigDecimal("100"));
        when(pagoRepository.save(any(PagoMaquina.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pagoRepository.sumMontoPagadoByPrestamoAndTipoPago(3L, Pago.TipoPago.CAPITAL))
                .thenReturn(BigDecimal.ZERO);
        when(pagoRepository.sumMontoPagadoByPrestamoAndTipoPago(3L, Pago.TipoPago.INTERES))
                .thenReturn(BigDecimal.ZERO);

        PagoMaquina descuento = service.aplicarDescuentoInteres(3L, new BigDecimal("50"));

        assertTrue(descuento.getEsDescuento());
        assertEquals(Pago.TipoPago.INTERES, descuento.getTipoPago());
        assertEquals(new BigDecimal("50"), descuento.getMontoPagado());
    }

    @Test
    void rechazaDescuentoInvalidoOSuperiorAlSaldo() {
        PrestamoMaquina prestamo = prestamo(4L, "1000", EstadoPrestamo.ACTIVO);
        when(prestamoRepository.findById(4L)).thenReturn(Optional.of(prestamo));
        assertThrows(IllegalArgumentException.class, () -> service.aplicarDescuentoInteres(4L, BigDecimal.ZERO));

        when(calculoService.calcularInteresPendiente(eq(prestamo), any())).thenReturn(BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class,
                () -> service.aplicarDescuentoInteres(4L, BigDecimal.ONE));

        when(calculoService.calcularInteresPendiente(eq(prestamo), any())).thenReturn(new BigDecimal("10"));
        assertThrows(IllegalArgumentException.class,
                () -> service.aplicarDescuentoInteres(4L, new BigDecimal("11")));
    }

    @Test
    void resumePagosYActualizaPrestamoAEstadoVencido() {
        PrestamoMaquina prestamo = prestamo(5L, "1000", EstadoPrestamo.ACTIVO);
        when(prestamoRepository.findById(5L)).thenReturn(Optional.of(prestamo));
        when(pagoRepository.sumMontoPagadoByPrestamoAndTipoPago(5L, Pago.TipoPago.CAPITAL))
                .thenReturn(new BigDecimal("100"));
        when(pagoRepository.sumMontoPagadoByPrestamoAndTipoPago(5L, Pago.TipoPago.INTERES))
                .thenReturn(new BigDecimal("20"));
        when(calculoService.calcularInteresPendiente(eq(prestamo), any())).thenReturn(new BigDecimal("200"));

        Map<String, BigDecimal> resumen = service.obtenerResumenPagos(5L);

        assertEquals(new BigDecimal("900"), resumen.get("capitalPendiente"));
        assertEquals(new BigDecimal("200"), resumen.get("interesPendiente"));
        assertEquals(new BigDecimal("220"), resumen.get("interesTotal"));
        assertEquals(EstadoPrestamo.VENCIDO, prestamo.getEstadoPrestamo());
        verify(prestamoRepository, times(2)).save(prestamo);
    }

    @Test
    void reabrePrestamoVencidoYMarcaPagadoCuandoCapitalEstaCompleto() {
        PrestamoMaquina vencido = prestamo(6L, "1000", EstadoPrestamo.VENCIDO);
        when(prestamoRepository.findById(6L)).thenReturn(Optional.of(vencido));
        when(pagoRepository.sumMontoPagadoByPrestamoAndTipoPago(6L, Pago.TipoPago.CAPITAL))
                .thenReturn(new BigDecimal("500"));
        when(pagoRepository.sumMontoPagadoByPrestamoAndTipoPago(6L, Pago.TipoPago.INTERES))
                .thenReturn(null);
        when(calculoService.calcularInteresPendiente(eq(vencido), any())).thenReturn(new BigDecimal("-5"));

        Map<String, BigDecimal> reabierto = service.obtenerResumenPagos(6L);
        assertEquals(BigDecimal.ZERO, reabierto.get("interesPendiente"));
        assertEquals(EstadoPrestamo.PENDIENTE, vencido.getEstadoPrestamo());

        PrestamoMaquina pagado = prestamo(7L, "1000", EstadoPrestamo.ACTIVO);
        when(prestamoRepository.findById(7L)).thenReturn(Optional.of(pagado));
        when(pagoRepository.sumMontoPagadoByPrestamoAndTipoPago(7L, Pago.TipoPago.CAPITAL))
                .thenReturn(new BigDecimal("1000"));
        when(pagoRepository.sumMontoPagadoByPrestamoAndTipoPago(7L, Pago.TipoPago.INTERES))
                .thenReturn(BigDecimal.ZERO);
        when(calculoService.calcularInteresPendiente(eq(pagado), any())).thenReturn(BigDecimal.ZERO);

        service.obtenerResumenPagos(7L);
        assertEquals(EstadoPrestamo.PAGADO, pagado.getEstadoPrestamo());
        verify(prestamoRepository, times(3)).save(pagado);
    }

    @Test
    void exponeListasDePagosYRechazaPrestamoInexistenteAlConsultarResumen() {
        when(pagoRepository.findByPrestamoMaquinaIdPrestamoMaquinaOrderByFechaPagoDesc(8L))
                .thenReturn(List.of());
        when(pagoRepository.findAllPagosWithDetails()).thenReturn(List.of());
        assertTrue(service.obtenerPagosPorPrestamoMaquina(8L).isEmpty());
        assertTrue(service.obtenerTodosPagos().isEmpty());
        assertThrows(RuntimeException.class, () -> service.obtenerResumenPagos(8L));
    }

    private PrestamoMaquina prestamo(Long id, String monto, EstadoPrestamo estado) {
        PrestamoMaquina prestamo = new PrestamoMaquina();
        prestamo.setIdPrestamoMaquina(id);
        prestamo.setMontoPrestamo(new BigDecimal(monto));
        prestamo.setTasaInteres(new BigDecimal("5"));
        prestamo.setEstadoPrestamo(estado);
        return prestamo;
    }

    private PagoMaquina pago(PrestamoMaquina prestamo, String monto, Pago.TipoPago tipo) {
        PagoMaquina pago = new PagoMaquina();
        pago.setPrestamoMaquina(prestamo);
        pago.setMontoPagado(new BigDecimal(monto));
        pago.setTipoPago(tipo);
        return pago;
    }
}
