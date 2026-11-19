package control.prendario.service;

import control.prendario.DTO.CorreccionPagoDTO;
import control.prendario.model.*;
import control.prendario.repository.PagoMaquinaRepository;
import control.prendario.repository.PrestamoMaquinaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class PagoMaquinaService {
    @Autowired
    private PagoMaquinaRepository pagoMaquinaRepository;

    @Autowired
    private PrestamoMaquinaRepository prestamoMaquinaRepository;
    @Autowired
    private CalculoInteresMaquinaService calculoInteresMaquinaService;

    @Transactional
    public PagoMaquina crearPago(PagoMaquina pago) {
        PagoService.validarNuevoPago(
                pago == null ? null : pago.getPrestamoMaquina() == null ? null : pago.getPrestamoMaquina().getIdPrestamoMaquina(),
                pago == null ? null : pago.getMontoPagado(), pago == null ? null : pago.getTipoPago());
        PrestamoMaquina prestamoMaquina = prestamoMaquinaRepository.findById(pago.getPrestamoMaquina().getIdPrestamoMaquina())
                .orElseThrow(() -> new EntityNotFoundException("Préstamo de máquina no encontrado"));
        pago.setEstadoPago(EstadoPago.ACTIVO);
        pago.setEsDescuento(false);
        pago.setFechaCorreccion(null);
        pago.setMotivoCorreccion(null);
        pago.setCorregidoPorPagoId(null);
        pago.setReemplazaPagoId(null);

        // Calculate total paid amounts
        BigDecimal capitalPagado = pagoMaquinaRepository.sumMontoPagadoByPrestamoAndTipoPago(
                prestamoMaquina.getIdPrestamoMaquina(),
                Pago.TipoPago.CAPITAL
        );
        BigDecimal interesPagado = pagoMaquinaRepository.sumMontoPagadoByPrestamoAndTipoPago(
                prestamoMaquina.getIdPrestamoMaquina(),
                Pago.TipoPago.INTERES
        );

        // Null handling
        capitalPagado = capitalPagado != null ? capitalPagado : BigDecimal.ZERO;
        interesPagado = interesPagado != null ? interesPagado : BigDecimal.ZERO;

        // Add current payment
        if (pago.getTipoPago() == Pago.TipoPago.CAPITAL) {
            capitalPagado = capitalPagado.add(pago.getMontoPagado());
        } else if (pago.getTipoPago() == Pago.TipoPago.INTERES) {
            interesPagado = interesPagado.add(pago.getMontoPagado());
        }

        // Calculate total interest
        BigDecimal interesTotal = prestamoMaquina.getMontoPrestamo()
                .multiply(prestamoMaquina.getTasaInteres().multiply(new BigDecimal("3")))
                .divide(new BigDecimal("100"));

        // Check if loan is fully paid
        if (capitalPagado.compareTo(prestamoMaquina.getMontoPrestamo()) >= 0 &&
                interesPagado.compareTo(interesTotal) >= 0) {
            prestamoMaquina.setEstadoPrestamo(EstadoPrestamo.PAGADO);
            prestamoMaquinaRepository.save(prestamoMaquina);
        }

        return pagoMaquinaRepository.save(pago);
    }

    @Transactional
    public PagoMaquina aplicarDescuentoInteres(Long idPrestamoMaquina, BigDecimal montoInteres) {
        PrestamoMaquina prestamoMaquina = prestamoMaquinaRepository.findById(idPrestamoMaquina)
                .orElseThrow(() -> new RuntimeException("Préstamo de máquina no encontrado"));

        if (montoInteres == null || montoInteres.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del descuento debe ser mayor que cero");
        }

        BigDecimal interesPendiente = calculoInteresMaquinaService
                .calcularInteresPendiente(prestamoMaquina, LocalDate.now());

        if (interesPendiente.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El préstamo no tiene intereses pendientes");
        }
        if (montoInteres.compareTo(interesPendiente) > 0) {
            throw new IllegalArgumentException("El descuento no puede exceder el interés pendiente");
        }

        PagoMaquina pagoDescuento = new PagoMaquina();
        pagoDescuento.setPrestamoMaquina(prestamoMaquina);
        pagoDescuento.setMontoPagado(montoInteres);
        pagoDescuento.setTipoPago(Pago.TipoPago.INTERES);
        pagoDescuento.setObservaciones("Descuento aplicado al interés");
        pagoDescuento.setEsDescuento(true);

        PagoMaquina descuentoGuardado = pagoMaquinaRepository.save(pagoDescuento);
        obtenerResumenPagos(idPrestamoMaquina);

        return descuentoGuardado;
    }

    @Transactional
    public PagoMaquina corregirPago(Long idPago, CorreccionPagoDTO correccion) {
        PagoMaquina original = pagoMaquinaRepository.findByIdForUpdate(idPago)
                .orElseThrow(() -> new EntityNotFoundException("Pago de maquinaria no encontrado"));
        if (original.getEstadoPago() == EstadoPago.CORREGIDO) {
            throw new IllegalArgumentException("El pago ya fue corregido");
        }
        validarCorreccion(correccion);

        PagoMaquina reemplazo = new PagoMaquina();
        reemplazo.setPrestamoMaquina(original.getPrestamoMaquina());
        reemplazo.setFechaPago(original.getFechaPago());
        reemplazo.setMontoPagado(correccion.getMontoPagado());
        reemplazo.setTipoPago(correccion.getTipoPago());
        reemplazo.setMetodoPago(correccion.getMetodoPago());
        reemplazo.setObservaciones(correccion.getObservaciones());
        reemplazo.setReemplazaPagoId(original.getIdPagoMaquina());
        reemplazo.setMotivoCorreccion(correccion.getMotivo().trim());
        reemplazo.setEstadoPago(EstadoPago.ACTIVO);
        reemplazo.setEsDescuento(Boolean.TRUE.equals(original.getEsDescuento()));
        reemplazo = pagoMaquinaRepository.save(reemplazo);

        original.setEstadoPago(EstadoPago.CORREGIDO);
        original.setFechaCorreccion(java.time.LocalDateTime.now());
        original.setMotivoCorreccion(correccion.getMotivo().trim());
        original.setCorregidoPorPagoId(reemplazo.getIdPagoMaquina());
        pagoMaquinaRepository.save(original);

        obtenerResumenPagos(original.getPrestamoMaquina().getIdPrestamoMaquina());
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


    public Map<String, BigDecimal> obtenerResumenPagos(Long idPrestamoMaquina) {
        PrestamoMaquina prestamoMaquina = prestamoMaquinaRepository.findById(idPrestamoMaquina)
                .orElseThrow(() -> new RuntimeException("Préstamo de máquina no encontrado"));

        // Calculate totals
        BigDecimal capitalPagado = pagoMaquinaRepository.sumMontoPagadoByPrestamoAndTipoPago(
                idPrestamoMaquina, Pago.TipoPago.CAPITAL
        );
        BigDecimal interesPagado = pagoMaquinaRepository.sumMontoPagadoByPrestamoAndTipoPago(
                idPrestamoMaquina, Pago.TipoPago.INTERES
        );

        // Null handling
        capitalPagado = capitalPagado != null ? capitalPagado : BigDecimal.ZERO;
        interesPagado = interesPagado != null ? interesPagado : BigDecimal.ZERO;

        // Calculate pending amounts
        BigDecimal capitalPendiente = prestamoMaquina.getMontoPrestamo().subtract(capitalPagado);

        // Calcular el interés pendiente
        BigDecimal interesPendiente = calculoInteresMaquinaService.calcularInteresPendiente(
                prestamoMaquina,
                LocalDate.now()
        );

        if (interesPendiente.compareTo(BigDecimal.ZERO) < 0) {
            interesPendiente = BigDecimal.ZERO;
        }

        // Recalculate loan state after payment corrections as well as ordinary payments.
        if (capitalPendiente.compareTo(BigDecimal.ZERO) <= 0) {
            prestamoMaquina.setEstadoPrestamo(EstadoPrestamo.PAGADO);
            prestamoMaquinaRepository.save(prestamoMaquina);
        } else if (interesPendiente.compareTo(capitalPendiente.multiply(BigDecimal.valueOf(0.15))) > 0
                && (EstadoPrestamo.PENDIENTE.equals(prestamoMaquina.getEstadoPrestamo())
                || EstadoPrestamo.ACTIVO.equals(prestamoMaquina.getEstadoPrestamo())
                || EstadoPrestamo.PAGADO.equals(prestamoMaquina.getEstadoPrestamo()))) {
            prestamoMaquina.setEstadoPrestamo(EstadoPrestamo.VENCIDO);
            prestamoMaquinaRepository.save(prestamoMaquina);
        } else if (EstadoPrestamo.VENCIDO.equals(prestamoMaquina.getEstadoPrestamo())
                || EstadoPrestamo.PAGADO.equals(prestamoMaquina.getEstadoPrestamo())) {
            prestamoMaquina.setEstadoPrestamo(EstadoPrestamo.PENDIENTE);
            prestamoMaquinaRepository.save(prestamoMaquina);
        }

        //Calcular interes pendiente
        BigDecimal interesTotal = interesPagado.add(interesPendiente);

        if ( capitalPendiente.compareTo(BigDecimal.ZERO) == 0){
            prestamoMaquina.setEstadoPrestamo(EstadoPrestamo.PAGADO);
            prestamoMaquinaRepository.save(prestamoMaquina);
        }
        
        // Create summary map
        Map<String, BigDecimal> resumen = new HashMap<>();
        resumen.put("capitalTotal", prestamoMaquina.getMontoPrestamo());
        resumen.put("interesTotal", interesTotal);
        resumen.put("capitalPagado", capitalPagado);
        resumen.put("interesPagado", interesPagado);
        resumen.put("capitalPendiente", capitalPendiente);
        resumen.put("interesPendiente", interesPendiente);

        prestamoMaquina.setInteresTotal(interesTotal);
        prestamoMaquina.setCapitalPagado(capitalPagado);
        prestamoMaquina.setInteresPagado(interesPagado);
        prestamoMaquina.setCapitalPendiente(capitalPendiente);
        prestamoMaquina.setInteresPendiente(interesPendiente);
        prestamoMaquinaRepository.save(prestamoMaquina);

        return resumen;
    }


    public List<PagoMaquina> obtenerPagosPorPrestamoMaquina(Long idPrestamoMaquina) {
        return pagoMaquinaRepository.findByPrestamoMaquinaIdPrestamoMaquinaOrderByFechaPagoDesc(idPrestamoMaquina);
    }

    public List<PagoMaquina> obtenerTodosPagos() {
        return pagoMaquinaRepository.findAllPagosWithDetails();
    }
}
