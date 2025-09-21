package control.prendario.service;

import control.prendario.model.*;
import control.prendario.repository.PagoMaquinaRepository;
import control.prendario.repository.PrestamoMaquinaRepository;
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
        PrestamoMaquina prestamoMaquina = prestamoMaquinaRepository.findById(pago.getPrestamoMaquina().getIdPrestamoMaquina())
                .orElseThrow(() -> new RuntimeException("Préstamo de máquina no encontrado"));

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

        //Si el prestamo esta saldado el estado pasa a Pagado
        if (!EstadoPrestamo.PAGADO.equals(prestamoMaquina.getEstadoPrestamo()) && capitalPendiente.compareTo(BigDecimal.ZERO) == 0) {
            prestamoMaquina.setEstadoPrestamo(EstadoPrestamo.PAGADO);
            prestamoMaquinaRepository.save(prestamoMaquina);
        }
        //Si el prestamo supera tiene una deuda mayor pasa a estado vencido
        if ( (interesPendiente.compareTo(capitalPendiente.multiply(BigDecimal.valueOf(0.15))) > 0) && (EstadoPrestamo.PENDIENTE.equals(prestamoMaquina.getEstadoPrestamo()) || EstadoPrestamo.ACTIVO.equals(prestamoMaquina.getEstadoPrestamo()) )) {
            prestamoMaquina.setEstadoPrestamo(EstadoPrestamo.VENCIDO);
            prestamoMaquinaRepository.save(prestamoMaquina);
        }else if (EstadoPrestamo.VENCIDO.equals(prestamoMaquina.getEstadoPrestamo()) && (interesPendiente.compareTo(capitalPendiente.multiply(BigDecimal.valueOf(0.15))) <= 0) ){
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
