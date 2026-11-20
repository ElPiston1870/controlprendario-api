package control.prendario.service;

import control.prendario.model.EstadoPago;
import control.prendario.model.Pago;
import control.prendario.model.Prestamo;
import control.prendario.repository.PagoRepository;
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
class CalculoInteresServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @InjectMocks
    private CalculoInteresService service;

    @Test
    void cp09CalculaUnPeriodoParaPrestamoDeUnMillonAlCincoPorCiento() {
        Prestamo prestamo = prestamoReferencia();
        when(pagoRepository.findByPrestamoIdPrestamoOrderByFechaPagoDesc(22L)).thenReturn(List.of());

        BigDecimal interes = service.calcularInteresPendiente(prestamo, LocalDate.of(2026, 1, 6));

        assertEquals(new BigDecimal("50000"), interes);
    }

    @Test
    void cp10CalculaDosPeriodosSinAbonos() {
        Prestamo prestamo = prestamoReferencia();
        when(pagoRepository.findByPrestamoIdPrestamoOrderByFechaPagoDesc(22L)).thenReturn(List.of());

        BigDecimal interes = service.calcularInteresPendiente(prestamo, LocalDate.of(2026, 2, 6));

        assertEquals(new BigDecimal("100000"), interes);
    }

    @Test
    void cp11AplicaAbonoDeCapitalDeDoscientosMilDesdeElPeriodoSiguiente() {
        Prestamo prestamo = prestamoReferencia();
        Pago abonoCapital = pago(Pago.TipoPago.CAPITAL, "200000",
                LocalDate.of(2026, 1, 20), EstadoPago.ACTIVO);
        when(pagoRepository.findByPrestamoIdPrestamoOrderByFechaPagoDesc(22L))
                .thenReturn(List.of(abonoCapital));

        BigDecimal interes = service.calcularInteresPendiente(prestamo, LocalDate.of(2026, 2, 6));
        BigDecimal capitalPendiente = prestamo.getMontoPrestamo().subtract(abonoCapital.getMontoPagado());

        assertEquals(new BigDecimal("800000"), capitalPendiente);
        assertEquals(new BigDecimal("90000"), interes);
    }

    @Test
    void cp12RestaPagoDeInteresesYConservaElCapitalPendiente() {
        Prestamo prestamo = prestamoReferencia();
        Pago abonoCapital = pago(Pago.TipoPago.CAPITAL, "200000",
                LocalDate.of(2026, 1, 20), EstadoPago.ACTIVO);
        Pago pagoInteres = pago(Pago.TipoPago.INTERES, "50000",
                LocalDate.of(2026, 1, 25), EstadoPago.ACTIVO);
        when(pagoRepository.findByPrestamoIdPrestamoOrderByFechaPagoDesc(22L))
                .thenReturn(List.of(abonoCapital, pagoInteres));

        BigDecimal interesPendiente = service.calcularInteresPendiente(
                prestamo, LocalDate.of(2026, 2, 6));
        BigDecimal capitalPendiente = prestamo.getMontoPrestamo().subtract(abonoCapital.getMontoPagado());

        assertEquals(new BigDecimal("800000"), capitalPendiente);
        assertEquals(new BigDecimal("40000"), interesPendiente);
    }

    @Test
    void calculaInteresesConPagosDeCapitalYInteresesYOmitePagosCorregidos() {
        Prestamo prestamo = new Prestamo();
        prestamo.setIdPrestamo(8L);
        prestamo.setMontoPrestamo(new BigDecimal("1000"));
        prestamo.setTasaInteres(new BigDecimal("10"));
        prestamo.setFechaPrestamo(LocalDateTime.of(2025, 1, 15, 10, 0));

        Pago abonoCapital = pago(Pago.TipoPago.CAPITAL, "200", LocalDate.of(2025, 2, 15), EstadoPago.ACTIVO);
        Pago pagoInteres = pago(Pago.TipoPago.INTERES, "50", LocalDate.of(2025, 2, 10), EstadoPago.ACTIVO);
        Pago pagoCorregido = pago(Pago.TipoPago.INTERES, "1000", LocalDate.of(2025, 2, 10), EstadoPago.CORREGIDO);
        when(pagoRepository.findByPrestamoIdPrestamoOrderByFechaPagoDesc(8L))
                .thenReturn(List.of(abonoCapital, pagoInteres, pagoCorregido));

        BigDecimal interes = service.calcularInteresPendiente(prestamo, LocalDate.of(2025, 3, 15));

        assertEquals(new BigDecimal("230"), interes);
    }

    @Test
    void calculaUnMesCuandoPrestamoYConsultaSonDelMismoMes() {
        Prestamo prestamo = new Prestamo();
        prestamo.setIdPrestamo(9L);
        prestamo.setMontoPrestamo(new BigDecimal("500"));
        prestamo.setTasaInteres(new BigDecimal("5"));
        prestamo.setFechaPrestamo(LocalDateTime.of(2025, 4, 1, 0, 0));
        when(pagoRepository.findByPrestamoIdPrestamoOrderByFechaPagoDesc(9L)).thenReturn(List.of());

        assertEquals(new BigDecimal("25"),
                service.calcularInteresPendiente(prestamo, LocalDate.of(2025, 4, 20)));
    }

    @Test
    void rechazaFechaAnteriorAlInicioDelPrestamo() {
        Prestamo prestamo = new Prestamo();
        prestamo.setIdPrestamo(10L);
        prestamo.setMontoPrestamo(BigDecimal.ONE);
        prestamo.setTasaInteres(BigDecimal.ONE);
        prestamo.setFechaPrestamo(LocalDateTime.of(2025, 4, 2, 0, 0));

        assertThrows(IllegalArgumentException.class,
                () -> service.calcularInteresPendiente(prestamo, LocalDate.of(2025, 4, 1)));
    }

    private Prestamo prestamoReferencia() {
        Prestamo prestamo = new Prestamo();
        prestamo.setIdPrestamo(22L);
        prestamo.setMontoPrestamo(new BigDecimal("1000000"));
        prestamo.setTasaInteres(new BigDecimal("5"));
        prestamo.setFechaPrestamo(LocalDateTime.of(2026, 1, 5, 0, 0));
        return prestamo;
    }

    private Pago pago(Pago.TipoPago tipo, String monto, LocalDate fecha, EstadoPago estado) {
        Pago pago = new Pago();
        pago.setTipoPago(tipo);
        pago.setMontoPagado(new BigDecimal(monto));
        pago.setFechaPago(fecha.atStartOfDay());
        pago.setEstadoPago(estado);
        return pago;
    }
}
