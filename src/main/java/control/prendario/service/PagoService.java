package control.prendario.service;

import control.prendario.DTO.CorreccionPagoDTO;
import control.prendario.model.EstadoPago;
import control.prendario.model.EstadoPrestamo;
import control.prendario.model.Pago;
import control.prendario.model.Prestamo;
import control.prendario.repository.PagoRepository;
import control.prendario.repository.PrestamoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
        pago.setEstadoPago(EstadoPago.ACTIVO);
        pago.setFechaCorreccion(null);
        pago.setMotivoCorreccion(null);
        pago.setCorregidoPorPagoId(null);
        pago.setReemplazaPagoId(null);

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

    @Transactional
    public Pago corregirPago(Long idPago, CorreccionPagoDTO correccion) {
        Pago original = pagoRepository.findByIdForUpdate(idPago)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado"));
        if (original.getEstadoPago() == EstadoPago.CORREGIDO) {
            throw new IllegalArgumentException("El pago ya fue corregido");
        }
        validarCorreccion(correccion);

        Pago reemplazo = new Pago();
        reemplazo.setPrestamo(original.getPrestamo());
        reemplazo.setFechaPago(original.getFechaPago());
        reemplazo.setMontoPagado(correccion.getMontoPagado());
        reemplazo.setTipoPago(correccion.getTipoPago());
        reemplazo.setMetodoPago(correccion.getMetodoPago());
        reemplazo.setObservaciones(correccion.getObservaciones());
        reemplazo.setReemplazaPagoId(original.getIdPago());
        reemplazo.setMotivoCorreccion(correccion.getMotivo().trim());
        reemplazo.setEstadoPago(EstadoPago.ACTIVO);
        reemplazo = pagoRepository.save(reemplazo);

        original.setEstadoPago(EstadoPago.CORREGIDO);
        original.setFechaCorreccion(LocalDateTime.now());
        original.setMotivoCorreccion(correccion.getMotivo().trim());
        original.setCorregidoPorPagoId(reemplazo.getIdPago());
        pagoRepository.save(original);

        obtenerResumenPagos(original.getPrestamo().getIdPrestamo());
        return reemplazo;
    }

    private void validarCorreccion(CorreccionPagoDTO correccion) {
        if (correccion == null
                || correccion.getMontoPagado() == null
                || correccion.getMontoPagado().compareTo(BigDecimal.ZERO) <= 0
                || correccion.getTipoPago() == null
                || correccion.getMetodoPago() == null
                || correccion.getMotivo() == null
                || correccion.getMotivo().isBlank()) {
            throw new IllegalArgumentException("Debe indicar monto positivo, concepto, método de pago y motivo");
        }
    }

    @Transactional
    public Pago aplicarDescuentoInteres(Long idPrestamo, BigDecimal montoDescuento) {
        Prestamo prestamo = prestamoRepository.findById(idPrestamo)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        if (montoDescuento == null || montoDescuento.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del descuento debe ser mayor que cero");
        }

        BigDecimal interesPendiente = calculoInteresService.calcularInteresPendiente(prestamo, LocalDate.now());

        if (interesPendiente.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El préstamo no tiene intereses pendientes");
        }
        if (montoDescuento.compareTo(interesPendiente) > 0) {
            throw new IllegalArgumentException("El descuento no puede exceder el interés pendiente");
        }

        Pago pagoDescuento = new Pago();
        pagoDescuento.setPrestamo(prestamo);
        pagoDescuento.setMontoPagado(montoDescuento);
        pagoDescuento.setTipoPago(Pago.TipoPago.INTERES);
        pagoDescuento.setFechaPago(LocalDateTime.now());
        pagoDescuento.setObservaciones("Descuento aplicado al interés pendiente");

        Pago descuentoGuardado = pagoRepository.save(pagoDescuento);
        obtenerResumenPagos(idPrestamo);

        return descuentoGuardado;
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
        // Recalculate loan state after payment corrections as well as ordinary payments.
        if (capitalPendiente.compareTo(BigDecimal.ZERO) <= 0) {
            prestamo.setEstadoPrestamo(EstadoPrestamo.PAGADO);
            prestamoRepository.save(prestamo);
        } else if (interesPendiente.compareTo(capitalPendiente.multiply(BigDecimal.valueOf(0.15))) > 0
                && (EstadoPrestamo.PENDIENTE.equals(prestamo.getEstadoPrestamo())
                || EstadoPrestamo.ACTIVO.equals(prestamo.getEstadoPrestamo())
                || EstadoPrestamo.PAGADO.equals(prestamo.getEstadoPrestamo()))) {
            prestamo.setEstadoPrestamo(EstadoPrestamo.VENCIDO);
            prestamoRepository.save(prestamo);
        } else if (EstadoPrestamo.VENCIDO.equals(prestamo.getEstadoPrestamo())
                || EstadoPrestamo.PAGADO.equals(prestamo.getEstadoPrestamo())) {
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
