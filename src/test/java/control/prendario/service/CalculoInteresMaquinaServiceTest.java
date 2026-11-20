package control.prendario.service;

import control.prendario.model.EstadoPago;
import control.prendario.model.Pago;
import control.prendario.model.PagoMaquina;
import control.prendario.model.PrestamoMaquina;
import control.prendario.repository.PagoMaquinaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalculoInteresMaquinaServiceTest {

    @Mock
    private PagoMaquinaRepository pagoRepository;

    @InjectMocks
    private CalculoInteresMaquinaService service;

    @Test
    void calculaInteresesDeMaquinariaRestandoPagosAplicables() {
        PrestamoMaquina prestamo = new PrestamoMaquina();
        prestamo.setIdPrestamoMaquina(3L);
        prestamo.setMontoPrestamo(new BigDecimal("1000"));
        prestamo.setTasaInteres(new BigDecimal("10"));
        prestamo.setFechaPrestamo(LocalDateTime.of(2025, 1, 1, 0, 0));

        PagoMaquina capital = pago(Pago.TipoPago.CAPITAL, "200", LocalDate.of(2025, 2, 1), EstadoPago.ACTIVO);
        PagoMaquina interes = pago(Pago.TipoPago.INTERES, "30", LocalDate.of(2025, 2, 1), EstadoPago.ACTIVO);
        PagoMaquina corregido = pago(Pago.TipoPago.CAPITAL, "900", LocalDate.of(2025, 1, 15), EstadoPago.CORREGIDO);
        when(pagoRepository.findByPrestamoMaquinaIdPrestamoMaquinaOrderByFechaPagoDesc(3L))
                .thenReturn(List.of(capital, interes, corregido));

        assertEquals(new BigDecimal("150"),
                service.calcularInteresPendiente(prestamo, LocalDate.of(2025, 2, 1)));
    }

    @Test
    void rechazaUnaFechaAnteriorAlPrestamo() {
        PrestamoMaquina prestamo = new PrestamoMaquina();
        prestamo.setFechaPrestamo(LocalDateTime.of(2025, 1, 2, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> service.calcularInteresPendiente(prestamo, LocalDate.of(2025, 1, 1)));
    }

    private PagoMaquina pago(Pago.TipoPago tipo, String monto, LocalDate fecha, EstadoPago estado) {
        PagoMaquina pago = new PagoMaquina();
        pago.setTipoPago(tipo);
        pago.setMontoPagado(new BigDecimal(monto));
        pago.setFechaPago(fecha.atStartOfDay());
        pago.setEstadoPago(estado);
        return pago;
    }
}
