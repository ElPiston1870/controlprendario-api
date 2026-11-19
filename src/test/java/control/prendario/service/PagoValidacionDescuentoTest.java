package control.prendario.service;

import control.prendario.model.Pago;
import control.prendario.model.PagoMaquina;
import control.prendario.model.Prestamo;
import control.prendario.model.PrestamoMaquina;
import control.prendario.repository.PagoMaquinaRepository;
import control.prendario.repository.PagoRepository;
import control.prendario.repository.PrestamoMaquinaRepository;
import control.prendario.repository.PrestamoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Validación de pagos (HU-04) y marca de descuento (HU-07, RN-14).
 */
@ExtendWith(MockitoExtension.class)
class PagoValidacionDescuentoTest {

    @Mock
    private PagoRepository pagoRepository;
    @Mock
    private PrestamoRepository prestamoRepository;
    @Mock
    private CalculoInteresService calculoInteresService;
    @Mock
    private PagoMaquinaRepository pagoMaquinaRepository;
    @Mock
    private PrestamoMaquinaRepository prestamoMaquinaRepository;
    @Mock
    private CalculoInteresMaquinaService calculoInteresMaquinaService;

    @InjectMocks
    private PagoService pagoService;
    @InjectMocks
    private PagoMaquinaService pagoMaquinaService;

    @Test
    void rechazaPagoConMontoCeroONegativo() {
        Pago pago = pagoVehicular(BigDecimal.ZERO, Pago.TipoPago.CAPITAL);
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> pagoService.crearPago(pago));
        assertEquals("El monto del pago debe ser mayor que cero", error.getMessage());

        Pago negativo = pagoVehicular(new BigDecimal("-1"), Pago.TipoPago.CAPITAL);
        assertThrows(IllegalArgumentException.class, () -> pagoService.crearPago(negativo));
        verify(pagoRepository, never()).save(any());
    }

    @Test
    void rechazaPagoSinTipoOSinPrestamo() {
        assertThrows(IllegalArgumentException.class,
                () -> pagoService.crearPago(pagoVehicular(new BigDecimal("1000"), null)));
        Pago sinPrestamo = new Pago();
        sinPrestamo.setMontoPagado(new BigDecimal("1000"));
        sinPrestamo.setTipoPago(Pago.TipoPago.CAPITAL);
        assertThrows(IllegalArgumentException.class, () -> pagoService.crearPago(sinPrestamo));
    }

    @Test
    void rechazaPagoDeMaquinariaConMontoInvalido() {
        PagoMaquina pago = new PagoMaquina();
        PrestamoMaquina prestamo = new PrestamoMaquina();
        prestamo.setIdPrestamoMaquina(3L);
        pago.setPrestamoMaquina(prestamo);
        pago.setTipoPago(Pago.TipoPago.INTERES);
        pago.setMontoPagado(null);
        assertThrows(IllegalArgumentException.class, () -> pagoMaquinaService.crearPago(pago));
        verify(pagoMaquinaRepository, never()).save(any());
    }

    @Test
    void pagoNormalNoSeMarcaComoDescuentoAunqueElClienteLoEnvie() {
        Prestamo prestamo = prestamo();
        when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        Pago pago = pagoVehicular(new BigDecimal("50000"), Pago.TipoPago.INTERES);
        pago.setEsDescuento(true);
        Pago guardado = pagoService.crearPago(pago);

        assertFalse(guardado.getEsDescuento());
    }

    @Test
    void descuentoVehicularQuedaMarcadoParaExcluirloDelBalance() {
        Prestamo prestamo = prestamo();
        when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));
        when(calculoInteresService.calcularInteresPendiente(any(), any())).thenReturn(new BigDecimal("40000"));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        Pago descuento = pagoService.aplicarDescuentoInteres(1L, new BigDecimal("10000"));

        assertTrue(descuento.getEsDescuento());
        assertEquals(Pago.TipoPago.INTERES, descuento.getTipoPago());
        assertEquals(new BigDecimal("10000"), descuento.getMontoPagado());
    }

    @Test
    void descuentoDeMaquinariaQuedaMarcado() {
        PrestamoMaquina prestamo = new PrestamoMaquina();
        prestamo.setIdPrestamoMaquina(3L);
        prestamo.setMontoPrestamo(new BigDecimal("1000000"));
        prestamo.setTasaInteres(new BigDecimal("5"));
        prestamo.setFechaPrestamo(LocalDateTime.now().minusDays(40));
        when(prestamoMaquinaRepository.findById(3L)).thenReturn(Optional.of(prestamo));
        when(calculoInteresMaquinaService.calcularInteresPendiente(any(), any())).thenReturn(new BigDecimal("40000"));
        ArgumentCaptor<PagoMaquina> captor = ArgumentCaptor.forClass(PagoMaquina.class);
        when(pagoMaquinaRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        pagoMaquinaService.aplicarDescuentoInteres(3L, new BigDecimal("10000"));

        assertTrue(captor.getAllValues().get(0).getEsDescuento());
    }

    private Pago pagoVehicular(BigDecimal monto, Pago.TipoPago tipo) {
        Pago pago = new Pago();
        Prestamo prestamo = new Prestamo();
        prestamo.setIdPrestamo(1L);
        pago.setPrestamo(prestamo);
        pago.setMontoPagado(monto);
        pago.setTipoPago(tipo);
        pago.setMetodoPago(Pago.MetodoPago.EFECTIVO);
        return pago;
    }

    private Prestamo prestamo() {
        Prestamo prestamo = new Prestamo();
        prestamo.setIdPrestamo(1L);
        prestamo.setMontoPrestamo(new BigDecimal("1000000"));
        prestamo.setTasaInteres(new BigDecimal("5"));
        prestamo.setFechaPrestamo(LocalDateTime.now().minusDays(40));
        return prestamo;
    }
}
