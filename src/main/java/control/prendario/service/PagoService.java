package control.prendario.service;

import control.prendario.model.EstadoPrestamo;
import control.prendario.model.Pago;
import control.prendario.model.Prestamo;
import control.prendario.repository.PagoRepository;
import control.prendario.repository.PrestamoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class PagoService {
    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private CalculoInteresService calculoInteresService;

    @Transactional
    public Pago crearPago(Pago pago) {
        Prestamo prestamo = prestamoRepository.findById(pago.getPrestamo().getIdPrestamo())
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        // Obtener totales actuales
        BigDecimal capitalPagado = pagoRepository.sumMontoPagadoByPrestamoAndTipoPago(
                prestamo.getIdPrestamo(),
                Pago.TipoPago.CAPITAL
        );
        BigDecimal interesPagado = pagoRepository.sumMontoPagadoByPrestamoAndTipoPago(
                prestamo.getIdPrestamo(),
                Pago.TipoPago.INTERES
        );

        // Si son null, inicializar en 0
        if (capitalPagado == null) capitalPagado = BigDecimal.ZERO;
        if (interesPagado == null) interesPagado = BigDecimal.ZERO;

        // Agregar el pago actual según su tipo
        if (pago.getTipoPago() == Pago.TipoPago.CAPITAL) {
            capitalPagado = capitalPagado.add(pago.getMontoPagado());
        } else if (pago.getTipoPago() == Pago.TipoPago.INTERES) {
            interesPagado = interesPagado.add(pago.getMontoPagado());
        }

        // Calcular el interés total esperado
        BigDecimal interesTotal = prestamo.getMontoPrestamo()
                .multiply(prestamo.getTasaInteres().multiply(new BigDecimal("3")))
                .divide(new BigDecimal("100"));

        // Verificar si con este pago se completa la deuda
        if (capitalPagado.compareTo(prestamo.getMontoPrestamo()) >= 0 &&
                interesPagado.compareTo(interesTotal) >= 0) {

            prestamo.setEstadoPrestamo(EstadoPrestamo.PAGADO);
            prestamoRepository.save(prestamo);
        }

        return pagoRepository.save(pago);
    }

    public List<Pago> obtenerPagosPorPrestamo(Long idPrestamo) {
        return pagoRepository.findByPrestamoIdPrestamoOrderByFechaPagoDesc(idPrestamo);
    }

    public void obtenerResumenPagos(Long idPrestamo) {
        Prestamo prestamo = prestamoRepository.findById(idPrestamo)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        // Obtener totales pagados
        BigDecimal capitalPagado = pagoRepository.sumMontoPagadoByPrestamoAndTipoPago(
                idPrestamo,
                Pago.TipoPago.CAPITAL
        );
        BigDecimal interesPagado = pagoRepository.sumMontoPagadoByPrestamoAndTipoPago(
                idPrestamo,
                Pago.TipoPago.INTERES
        );

        // Si son null, inicializar en 0
        if (capitalPagado == null) capitalPagado = BigDecimal.ZERO;
        if (interesPagado == null) interesPagado = BigDecimal.ZERO;

        // Calcular pendientes
        BigDecimal capitalPendiente = prestamo.getMontoPrestamo().subtract(capitalPagado);

        // Calcular el interés pendiente
        BigDecimal interesPendiente = calculoInteresService.calcularInteresPendiente(
                prestamo,
                LocalDate.now()
        );

        if (interesPendiente.compareTo(BigDecimal.ZERO) < 0) {
            interesPendiente = BigDecimal.ZERO;
        }
        //Si el prestamo esta saldado el estado pasa a Pagado
        if (!EstadoPrestamo.PAGADO.equals(prestamo.getEstadoPrestamo()) && capitalPendiente.compareTo(BigDecimal.ZERO) == 0) {
            prestamo.setEstadoPrestamo(EstadoPrestamo.PAGADO);
            prestamoRepository.save(prestamo);
        }
        //Si el prestamo supera tiene una deuda mayor pasa a estado vencido
        if ( (interesPendiente.compareTo(capitalPendiente.multiply(BigDecimal.valueOf(0.15))) > 0) && EstadoPrestamo.PENDIENTE.equals(prestamo.getEstadoPrestamo())) {
            prestamo.setEstadoPrestamo(EstadoPrestamo.VENCIDO);
            prestamoRepository.save(prestamo);
        }else if (EstadoPrestamo.VENCIDO.equals(prestamo.getEstadoPrestamo()) && (interesPendiente.compareTo(capitalPendiente.multiply(BigDecimal.valueOf(0.15))) <= 0) ){
            prestamo.setEstadoPrestamo(EstadoPrestamo.PENDIENTE);
            prestamoRepository.save(prestamo);
        }

        // Calcular interés total (intereses pagados + pendientes)
        BigDecimal interesTotal = interesPagado.add(interesPendiente);

        prestamo.setInteresTotal(interesTotal);
        prestamo.setCapitalPagado(capitalPagado);
        prestamo.setInteresPagado(interesPagado);
        prestamo.setCapitalPendiente(capitalPendiente);
        prestamo.setInteresPendiente(interesPendiente);
        prestamoRepository.save(prestamo);

    }

    public List<Pago> buscarPagosPorCliente(String termino) {
        return pagoRepository.buscarPorCliente(termino);
    }

    public List<Pago> obtenerTodosPagos() {
        return pagoRepository.findAllPagosWithDetails();
    }
}
