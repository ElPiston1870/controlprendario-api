package control.prendario.payment.service;

import control.prendario.shared.query.BrowseQuery;
import control.prendario.shared.query.CatalogFilters;
import control.prendario.shared.query.PageResult;
import org.springframework.data.domain.Sort;
import control.prendario.payment.domain.MachineryPayment;
import control.prendario.payment.domain.Payment;
import control.prendario.payment.domain.PaymentStatus;
import control.prendario.payment.dto.PaymentCorrectionDTO;
import control.prendario.payment.repository.MachineryPaymentRepository;
import control.prendario.loan.domain.LoanStatus;
import control.prendario.loan.domain.MachineryLoan;
import control.prendario.loan.repository.MachineryLoanRepository;

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
public class MachineryPaymentService {
    @Autowired
    private MachineryPaymentRepository machineryPaymentRepository;

    @Autowired
    private MachineryLoanRepository machineryLoanRepository;
    @Autowired
    private MachineryInterestCalculationService machineryInterestCalculationService;

    @Transactional
    public MachineryPayment createPayment(MachineryPayment payment) {
        PaymentService.validateNewPayment(
                payment == null ? null : payment.getMachineryLoan() == null ? null : payment.getMachineryLoan().getMachineryLoanId(),
                payment == null ? null : payment.getAmountPaid(), payment == null ? null : payment.getPaymentType());
        MachineryLoan machineryLoan = machineryLoanRepository.findById(payment.getMachineryLoan().getMachineryLoanId())
                .orElseThrow(() -> new EntityNotFoundException("Préstamo de máquina no encontrado"));
        payment.setPaymentStatus(PaymentStatus.ACTIVO);
        payment.setDiscount(false);
        payment.setCorrectionDate(null);
        payment.setCorrectionReason(null);
        payment.setCorrectedByPaymentId(null);
        payment.setReplacesPaymentId(null);

        // Calculate total paid amounts
        BigDecimal paidPrincipal = machineryPaymentRepository.sumAmountPaidByLoanAndPaymentType(
                machineryLoan.getMachineryLoanId(),
                Payment.PaymentType.CAPITAL
        );
        BigDecimal paidInterest = machineryPaymentRepository.sumAmountPaidByLoanAndPaymentType(
                machineryLoan.getMachineryLoanId(),
                Payment.PaymentType.INTERES
        );

        // Null handling
        paidPrincipal = paidPrincipal != null ? paidPrincipal : BigDecimal.ZERO;
        paidInterest = paidInterest != null ? paidInterest : BigDecimal.ZERO;

        // Add current payment
        if (payment.getPaymentType() == Payment.PaymentType.CAPITAL) {
            paidPrincipal = paidPrincipal.add(payment.getAmountPaid());
        } else if (payment.getPaymentType() == Payment.PaymentType.INTERES) {
            paidInterest = paidInterest.add(payment.getAmountPaid());
        }

        // Calculate total interest
        BigDecimal totalInterest = machineryLoan.getLoanAmount()
                .multiply(machineryLoan.getInterestRate().multiply(new BigDecimal("3")))
                .divide(new BigDecimal("100"));

        // Check if loan is fully paid
        if (paidPrincipal.compareTo(machineryLoan.getLoanAmount()) >= 0 &&
                paidInterest.compareTo(totalInterest) >= 0) {
            machineryLoan.setLoanStatus(LoanStatus.PAGADO);
            machineryLoanRepository.save(machineryLoan);
        }

        return machineryPaymentRepository.save(payment);
    }

    @Transactional
    public MachineryPayment applyInterestDiscount(Long machineryLoanId, BigDecimal interestAmount) {
        MachineryLoan machineryLoan = machineryLoanRepository.findById(machineryLoanId)
                .orElseThrow(() -> new RuntimeException("Préstamo de máquina no encontrado"));

        if (interestAmount == null || interestAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del descuento debe ser mayor que cero");
        }

        BigDecimal pendingInterest = machineryInterestCalculationService
                .calculatePendingInterest(machineryLoan, LocalDate.now());

        if (pendingInterest.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El préstamo no tiene intereses pendientes");
        }
        if (interestAmount.compareTo(pendingInterest) > 0) {
            throw new IllegalArgumentException("El descuento no puede exceder el interés pendiente");
        }

        MachineryPayment discountPayment = new MachineryPayment();
        discountPayment.setMachineryLoan(machineryLoan);
        discountPayment.setAmountPaid(interestAmount);
        discountPayment.setPaymentType(Payment.PaymentType.INTERES);
        discountPayment.setNotes("Descuento aplicado al interés");
        discountPayment.setDiscount(true);

        MachineryPayment savedDiscount = machineryPaymentRepository.save(discountPayment);
        computePaymentSummary(machineryLoanId);

        return savedDiscount;
    }

    @Transactional
    public MachineryPayment correctPayment(Long paymentId, PaymentCorrectionDTO correction) {
        MachineryPayment original = machineryPaymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Pago de maquinaria no encontrado"));
        if (original.getPaymentStatus() == PaymentStatus.CORREGIDO) {
            throw new IllegalArgumentException("El pago ya fue corregido");
        }
        validateCorrection(correction);

        MachineryPayment replacement = new MachineryPayment();
        replacement.setMachineryLoan(original.getMachineryLoan());
        replacement.setPaymentDate(original.getPaymentDate());
        replacement.setAmountPaid(correction.getAmountPaid());
        replacement.setPaymentType(correction.getPaymentType());
        replacement.setPaymentMethod(correction.getPaymentMethod());
        replacement.setNotes(correction.getNotes());
        replacement.setReplacesPaymentId(original.getMachineryPaymentId());
        replacement.setCorrectionReason(correction.getReason().trim());
        replacement.setPaymentStatus(PaymentStatus.ACTIVO);
        replacement.setDiscount(Boolean.TRUE.equals(original.getDiscount()));
        replacement = machineryPaymentRepository.save(replacement);

        original.setPaymentStatus(PaymentStatus.CORREGIDO);
        original.setCorrectionDate(java.time.LocalDateTime.now());
        original.setCorrectionReason(correction.getReason().trim());
        original.setCorrectedByPaymentId(replacement.getMachineryPaymentId());
        machineryPaymentRepository.save(original);

        computePaymentSummary(original.getMachineryLoan().getMachineryLoanId());
        return replacement;
    }

    private void validateCorrection(PaymentCorrectionDTO correction) {
        if (correction == null
                || correction.getAmountPaid() == null
                || correction.getAmountPaid().compareTo(BigDecimal.ZERO) <= 0
                || correction.getPaymentType() == null
                || correction.getPaymentMethod() == null
                || correction.getReason() == null
                || correction.getReason().isBlank()) {
            throw new IllegalArgumentException("Debe indicar monto positivo, concepto, método de pago y motivo");
        }
    }

    public Map<String, BigDecimal> computePaymentSummary(Long machineryLoanId) {
        MachineryLoan machineryLoan = machineryLoanRepository.findById(machineryLoanId)
                .orElseThrow(() -> new RuntimeException("Préstamo de máquina no encontrado"));

        // Calculate totals
        BigDecimal paidPrincipal = machineryPaymentRepository.sumAmountPaidByLoanAndPaymentType(
                machineryLoanId, Payment.PaymentType.CAPITAL
        );
        BigDecimal paidInterest = machineryPaymentRepository.sumAmountPaidByLoanAndPaymentType(
                machineryLoanId, Payment.PaymentType.INTERES
        );

        // Null handling
        paidPrincipal = paidPrincipal != null ? paidPrincipal : BigDecimal.ZERO;
        paidInterest = paidInterest != null ? paidInterest : BigDecimal.ZERO;

        // Calculate pending amounts
        BigDecimal pendingPrincipal = machineryLoan.getLoanAmount().subtract(paidPrincipal);

        // Calcular el interés pendiente
        BigDecimal pendingInterest = machineryInterestCalculationService.calculatePendingInterest(
                machineryLoan,
                LocalDate.now()
        );

        if (pendingInterest.compareTo(BigDecimal.ZERO) < 0) {
            pendingInterest = BigDecimal.ZERO;
        }

        // Recalculate loan state after payment corrections as well as ordinary payments.
        if (pendingPrincipal.compareTo(BigDecimal.ZERO) <= 0) {
            machineryLoan.setLoanStatus(LoanStatus.PAGADO);
            machineryLoanRepository.save(machineryLoan);
        } else if (pendingInterest.compareTo(pendingPrincipal.multiply(BigDecimal.valueOf(0.15))) > 0
                && (LoanStatus.PENDIENTE.equals(machineryLoan.getLoanStatus())
                || LoanStatus.ACTIVO.equals(machineryLoan.getLoanStatus())
                || LoanStatus.PAGADO.equals(machineryLoan.getLoanStatus()))) {
            machineryLoan.setLoanStatus(LoanStatus.VENCIDO);
            machineryLoanRepository.save(machineryLoan);
        } else if (LoanStatus.VENCIDO.equals(machineryLoan.getLoanStatus())
                || LoanStatus.PAGADO.equals(machineryLoan.getLoanStatus())) {
            machineryLoan.setLoanStatus(LoanStatus.PENDIENTE);
            machineryLoanRepository.save(machineryLoan);
        }

        //Calcular interes pendiente
        BigDecimal totalInterest = paidInterest.add(pendingInterest);

        if ( pendingPrincipal.compareTo(BigDecimal.ZERO) == 0){
            machineryLoan.setLoanStatus(LoanStatus.PAGADO);
            machineryLoanRepository.save(machineryLoan);
        }
        
        // Create summary map
        Map<String, BigDecimal> summary = new HashMap<>();
        summary.put("capitalTotal", machineryLoan.getLoanAmount());
        summary.put("interesTotal", totalInterest);
        summary.put("capitalPagado", paidPrincipal);
        summary.put("interesPagado", paidInterest);
        summary.put("capitalPendiente", pendingPrincipal);
        summary.put("interesPendiente", pendingInterest);

        machineryLoan.setTotalInterest(totalInterest);
        machineryLoan.setPaidPrincipal(paidPrincipal);
        machineryLoan.setPaidInterest(paidInterest);
        machineryLoan.setPendingPrincipal(pendingPrincipal);
        machineryLoan.setPendingInterest(pendingInterest);
        machineryLoanRepository.save(machineryLoan);

        return summary;
    }

    public List<MachineryPayment> getPaymentsByMachineryLoan(Long machineryLoanId) {
        return machineryPaymentRepository.findByMachineryLoanMachineryLoanIdOrderByPaymentDateDesc(machineryLoanId);
    }

    public List<MachineryPayment> getAllPayments() {
        return machineryPaymentRepository.findAllPaymentsWithDetails();
    }

    public PageResult<MachineryPayment> pagePayments(BrowseQuery q) {
        return PageResult.from(machineryPaymentRepository.findAll(CatalogFilters.machineryPayments(q),
                q.page(Sort.by(Sort.Direction.DESC, "paymentDate", "machineryPaymentId"))));
    }
}
