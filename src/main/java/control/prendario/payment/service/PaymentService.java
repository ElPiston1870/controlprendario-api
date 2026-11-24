package control.prendario.payment.service;

import control.prendario.shared.query.BrowseQuery;
import control.prendario.shared.query.CatalogFilters;
import control.prendario.shared.query.PageResult;
import org.springframework.data.domain.Sort;
import control.prendario.payment.domain.Payment;
import control.prendario.payment.domain.PaymentStatus;
import control.prendario.payment.dto.PaymentCorrectionDTO;
import control.prendario.payment.repository.PaymentRepository;
import control.prendario.loan.domain.Loan;
import control.prendario.loan.repository.LoanRepository;
import control.prendario.loan.domain.LoanStatus;

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
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private InterestCalculationService interestCalculationService;

    @Transactional
    public Payment createPayment(Payment payment) {
        validateNewPayment(payment == null ? null : payment.getLoan() == null ? null : payment.getLoan().getLoanId(),
                payment == null ? null : payment.getAmountPaid(), payment == null ? null : payment.getPaymentType());
        Loan loan = loanRepository.findById(payment.getLoan().getLoanId())
                .orElseThrow(() -> new EntityNotFoundException("Préstamo no encontrado"));
        payment.setPaymentStatus(PaymentStatus.ACTIVO);
        payment.setDiscount(false);
        payment.setCorrectionDate(null);
        payment.setCorrectionReason(null);
        payment.setCorrectedByPaymentId(null);
        payment.setReplacesPaymentId(null);

        // Obtener totales actuales
        BigDecimal paidPrincipal = paymentRepository.sumAmountPaidByLoanAndPaymentType(
                loan.getLoanId(),
                Payment.PaymentType.CAPITAL
        );
        BigDecimal paidInterest = paymentRepository.sumAmountPaidByLoanAndPaymentType(
                loan.getLoanId(),
                Payment.PaymentType.INTERES
        );

        // Si son null, inicializar en 0
        if (paidPrincipal == null) paidPrincipal = BigDecimal.ZERO;
        if (paidInterest == null) paidInterest = BigDecimal.ZERO;

        // Agregar el pago actual según su tipo
        if (payment.getPaymentType() == Payment.PaymentType.CAPITAL) {
            paidPrincipal = paidPrincipal.add(payment.getAmountPaid());
        } else if (payment.getPaymentType() == Payment.PaymentType.INTERES) {
            paidInterest = paidInterest.add(payment.getAmountPaid());
        }

        // Calcular el interés total esperado
        BigDecimal totalInterest = loan.getLoanAmount()
                .multiply(loan.getInterestRate().multiply(new BigDecimal("3")))
                .divide(new BigDecimal("100"));

        // Verificar si con este pago se completa la deuda
        if (paidPrincipal.compareTo(loan.getLoanAmount()) >= 0 &&
                paidInterest.compareTo(totalInterest) >= 0) {

            loan.setLoanStatus(LoanStatus.PAGADO);
            loanRepository.save(loan);
        }

        return paymentRepository.save(payment);
    }

    public List<Payment> getPaymentsByLoan(Long loanId) {
        return paymentRepository.findByLoanLoanIdOrderByPaymentDateDesc(loanId);
    }

    @Transactional
    public Payment correctPayment(Long paymentId, PaymentCorrectionDTO correction) {
        Payment original = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado"));
        if (original.getPaymentStatus() == PaymentStatus.CORREGIDO) {
            throw new IllegalArgumentException("El pago ya fue corregido");
        }
        validateCorrection(correction);

        Payment replacement = new Payment();
        replacement.setLoan(original.getLoan());
        replacement.setPaymentDate(original.getPaymentDate());
        replacement.setAmountPaid(correction.getAmountPaid());
        replacement.setPaymentType(correction.getPaymentType());
        replacement.setPaymentMethod(correction.getPaymentMethod());
        replacement.setNotes(correction.getNotes());
        replacement.setReplacesPaymentId(original.getPaymentId());
        replacement.setCorrectionReason(correction.getReason().trim());
        replacement.setPaymentStatus(PaymentStatus.ACTIVO);
        replacement.setDiscount(Boolean.TRUE.equals(original.getDiscount()));
        replacement = paymentRepository.save(replacement);

        original.setPaymentStatus(PaymentStatus.CORREGIDO);
        original.setCorrectionDate(LocalDateTime.now());
        original.setCorrectionReason(correction.getReason().trim());
        original.setCorrectedByPaymentId(replacement.getPaymentId());
        paymentRepository.save(original);

        computePaymentSummary(original.getLoan().getLoanId());
        return replacement;
    }

    static void validateNewPayment(Long loanId, BigDecimal amount, Payment.PaymentType paymentType) {
        if (loanId == null) {
            throw new IllegalArgumentException("Debe indicar el préstamo al que pertenece el pago");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor que cero");
        }
        if (paymentType == null) {
            throw new IllegalArgumentException("Debe indicar si el pago es a capital o a intereses");
        }
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

    @Transactional
    public Payment applyInterestDiscount(Long loanId, BigDecimal discountAmount) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del descuento debe ser mayor que cero");
        }

        BigDecimal pendingInterest = interestCalculationService.calculatePendingInterest(loan, LocalDate.now());

        if (pendingInterest.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El préstamo no tiene intereses pendientes");
        }
        if (discountAmount.compareTo(pendingInterest) > 0) {
            throw new IllegalArgumentException("El descuento no puede exceder el interés pendiente");
        }

        Payment discountPayment = new Payment();
        discountPayment.setLoan(loan);
        discountPayment.setAmountPaid(discountAmount);
        discountPayment.setPaymentType(Payment.PaymentType.INTERES);
        discountPayment.setPaymentDate(LocalDateTime.now());
        discountPayment.setNotes("Descuento aplicado al interés pendiente");
        discountPayment.setDiscount(true);

        Payment savedDiscount = paymentRepository.save(discountPayment);
        computePaymentSummary(loanId);

        return savedDiscount;
    }

    public void computePaymentSummary(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        // Obtener totales pagados
        BigDecimal paidPrincipal = paymentRepository.sumAmountPaidByLoanAndPaymentType(
                loanId,
                Payment.PaymentType.CAPITAL
        );
        BigDecimal paidInterest = paymentRepository.sumAmountPaidByLoanAndPaymentType(
                loanId,
                Payment.PaymentType.INTERES
        );

        // Si son null, inicializar en 0
        if (paidPrincipal == null) paidPrincipal = BigDecimal.ZERO;
        if (paidInterest == null) paidInterest = BigDecimal.ZERO;

        // Calcular pendientes
        BigDecimal pendingPrincipal = loan.getLoanAmount().subtract(paidPrincipal);

        // Calcular el interés pendiente
        BigDecimal pendingInterest = interestCalculationService.calculatePendingInterest(
                loan,
                LocalDate.now()
        );

        if (pendingInterest.compareTo(BigDecimal.ZERO) < 0) {
            pendingInterest = BigDecimal.ZERO;
        }
        // Recalculate loan state after payment corrections as well as ordinary payments.
        if (pendingPrincipal.compareTo(BigDecimal.ZERO) <= 0) {
            loan.setLoanStatus(LoanStatus.PAGADO);
            loanRepository.save(loan);
        } else if (pendingInterest.compareTo(pendingPrincipal.multiply(BigDecimal.valueOf(0.15))) > 0
                && (LoanStatus.PENDIENTE.equals(loan.getLoanStatus())
                || LoanStatus.ACTIVO.equals(loan.getLoanStatus())
                || LoanStatus.PAGADO.equals(loan.getLoanStatus()))) {
            loan.setLoanStatus(LoanStatus.VENCIDO);
            loanRepository.save(loan);
        } else if (LoanStatus.VENCIDO.equals(loan.getLoanStatus())
                || LoanStatus.PAGADO.equals(loan.getLoanStatus())) {
            loan.setLoanStatus(LoanStatus.PENDIENTE);
            loanRepository.save(loan);
        }

        // Calcular interés total (intereses pagados + pendientes)
        BigDecimal totalInterest = paidInterest.add(pendingInterest);

        loan.setTotalInterest(totalInterest);
        loan.setPaidPrincipal(paidPrincipal);
        loan.setPaidInterest(paidInterest);
        loan.setPendingPrincipal(pendingPrincipal);
        loan.setPendingInterest(pendingInterest);
        loanRepository.save(loan);

    }

    public List<Payment> searchPaymentsByClient(String term) {
        return paymentRepository.searchByClient(term);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAllPaymentsWithDetails();
    }

    public PageResult<Payment> pagePayments(BrowseQuery q) {
        return PageResult.from(paymentRepository.findAll(CatalogFilters.payments(q),
                q.page(Sort.by(Sort.Direction.DESC, "paymentDate", "paymentId"))));
    }
}
