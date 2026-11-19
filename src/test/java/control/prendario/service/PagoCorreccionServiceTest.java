package control.prendario.service;

import control.prendario.DTO.CorreccionPagoDTO;
import control.prendario.model.EstadoPago;
import control.prendario.model.EstadoPrestamo;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoCorreccionServiceTest {

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
    void corrigePagoVehicularConservandoElOriginalYActualizandoResumen() {
        Prestamo prestamo = crearPrestamo();
        Pago original = new Pago();
        original.setIdPago(5L);
        original.setPrestamo(prestamo);
        original.setFechaPago(LocalDateTime.of(2026, 1, 10, 9, 0));
        original.setMontoPagado(new BigDecimal("100"));
        original.setTipoPago(Pago.TipoPago.CAPITAL);

        when(pagoRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(original));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> {
            Pago pago = invocation.getArgument(0);
            if (pago.getIdPago() == null) {
                pago.setIdPago(6L);
            }
            return pago;
        });
        when(prestamoRepository.findById(2L)).thenReturn(Optional.of(prestamo));
        when(pagoRepository.sumMontoPagadoByPrestamoAndTipoPago(2L, Pago.TipoPago.CAPITAL))
                .thenReturn(new BigDecimal("250"));
        when(pagoRepository.sumMontoPagadoByPrestamoAndTipoPago(2L, Pago.TipoPago.INTERES))
                .thenReturn(BigDecimal.ZERO);
        when(calculoInteresService.calcularInteresPendiente(any(), any())).thenReturn(BigDecimal.ZERO);

        Pago reemplazo = pagoService.corregirPago(5L, crearCorreccion("250"));

        assertEquals(EstadoPago.CORREGIDO, original.getEstadoPago());
        assertEquals("Error de digitación", original.getMotivoCorreccion());
        assertEquals(6L, original.getCorregidoPorPagoId());
        assertEquals(5L, reemplazo.getReemplazaPagoId());
        assertEquals(new BigDecimal("250"), reemplazo.getMontoPagado());
        assertEquals(original.getFechaPago(), reemplazo.getFechaPago());
        verify(pagoRepository).save(original);
    }

    @Test
    void noPermiteCorregirPagoCorregidoNuevamente() {
        Pago original = new Pago();
        original.setEstadoPago(EstadoPago.CORREGIDO);
        when(pagoRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(original));

        assertThrows(IllegalArgumentException.class,
                () -> pagoService.corregirPago(5L, crearCorreccion("250")));
    }

    @Test
    void corrigePagoDeMaquinariaConservandoElOriginal() {
        PrestamoMaquina prestamo = new PrestamoMaquina();
        prestamo.setIdPrestamoMaquina(3L);
        prestamo.setMontoPrestamo(new BigDecimal("10000"));
        prestamo.setEstadoPrestamo(EstadoPrestamo.ACTIVO);

        PagoMaquina original = new PagoMaquina();
        original.setIdPagoMaquina(8L);
        original.setPrestamoMaquina(prestamo);
        original.setFechaPago(LocalDateTime.of(2026, 1, 10, 9, 0));
        original.setMontoPagado(new BigDecimal("100"));
        original.setTipoPago(Pago.TipoPago.CAPITAL);

        when(pagoMaquinaRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(original));
        when(pagoMaquinaRepository.save(any(PagoMaquina.class))).thenAnswer(invocation -> {
            PagoMaquina pago = invocation.getArgument(0);
            if (pago.getIdPagoMaquina() == null) {
                pago.setIdPagoMaquina(9L);
            }
            return pago;
        });
        when(prestamoMaquinaRepository.findById(3L)).thenReturn(Optional.of(prestamo));
        when(pagoMaquinaRepository.sumMontoPagadoByPrestamoAndTipoPago(3L, Pago.TipoPago.CAPITAL))
                .thenReturn(new BigDecimal("250"));
        when(pagoMaquinaRepository.sumMontoPagadoByPrestamoAndTipoPago(3L, Pago.TipoPago.INTERES))
                .thenReturn(BigDecimal.ZERO);
        when(calculoInteresMaquinaService.calcularInteresPendiente(any(), any())).thenReturn(BigDecimal.ZERO);

        PagoMaquina reemplazo = pagoMaquinaService.corregirPago(8L, crearCorreccion("250"));

        assertEquals(EstadoPago.CORREGIDO, original.getEstadoPago());
        assertEquals(9L, original.getCorregidoPorPagoId());
        assertEquals(8L, reemplazo.getReemplazaPagoId());
        assertEquals(new BigDecimal("250"), reemplazo.getMontoPagado());
        verify(pagoMaquinaRepository).save(original);
    }

    private Prestamo crearPrestamo() {
        Prestamo prestamo = new Prestamo();
        prestamo.setIdPrestamo(2L);
        prestamo.setMontoPrestamo(new BigDecimal("10000"));
        prestamo.setEstadoPrestamo(EstadoPrestamo.ACTIVO);
        return prestamo;
    }

    private CorreccionPagoDTO crearCorreccion(String monto) {
        CorreccionPagoDTO correccion = new CorreccionPagoDTO();
        correccion.setMontoPagado(new BigDecimal(monto));
        correccion.setTipoPago(Pago.TipoPago.CAPITAL);
        correccion.setMetodoPago(Pago.MetodoPago.EFECTIVO);
        correccion.setMotivo("Error de digitación");
        return correccion;
    }
}
