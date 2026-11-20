package control.prendario.payment;

import control.prendario.loan.LoanStatus;
import control.prendario.loan.MachineryLoan;
import control.prendario.loan.MachineryLoanRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MachineryPaymentServiceTest {

    @Mock private MachineryPaymentRepository paymentRepository;
    @Mock private MachineryLoanRepository loanRepository;
    @Mock private MachineryInterestCalculationService calculationService;
    @InjectMocks private MachineryPaymentService service;

    @Test
    void createsValidPaymentAndMarksLoanPaidWhenFullyCovered() {
        MachineryLoan loan = loan(1L, "1000", LoanStatus.ACTIVO);
        MachineryPayment payment = payment(loan, "1000", Payment.PaymentType.CAPITAL);
        payment.setPaymentStatus(PaymentStatus.CORREGIDO);
        payment.setDiscount(true);
        payment.setCorrectionReason("previo");
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(paymentRepository.sumAmountPaidByLoanAndPaymentType(1L, Payment.PaymentType.CAPITAL))
                .thenReturn(BigDecimal.ZERO);
        when(paymentRepository.sumAmountPaidByLoanAndPaymentType(1L, Payment.PaymentType.INTERES))
                .thenReturn(new BigDecimal("150"));
        when(paymentRepository.save(payment)).thenReturn(payment);

        assertSame(payment, service.createPayment(payment));
        assertEquals(PaymentStatus.ACTIVO, payment.getPaymentStatus());
        assertFalse(payment.getDiscount());
        assertNull(payment.getCorrectionReason());
        assertEquals(LoanStatus.PAGADO, loan.getLoanStatus());
        verify(loanRepository).save(loan);
    }

    @Test
    void rejectsPaymentForUnknownLoanAndValidatesNulls() {
        MachineryPayment withoutLoan = new MachineryPayment();
        assertThrows(IllegalArgumentException.class, () -> service.createPayment(null));
        assertThrows(IllegalArgumentException.class, () -> service.createPayment(withoutLoan));

        MachineryPayment payment = payment(loan(2L, "1000", LoanStatus.ACTIVO), "100", Payment.PaymentType.CAPITAL);
        when(loanRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> service.createPayment(payment));
    }

    @Test
    void createsDiscountOnlyWithinPendingInterest() {
        MachineryLoan loan = loan(3L, "1000", LoanStatus.ACTIVO);
        when(loanRepository.findById(3L)).thenReturn(Optional.of(loan));
        when(calculationService.calculatePendingInterest(eq(loan), any())).thenReturn(new BigDecimal("100"));
        when(paymentRepository.save(any(MachineryPayment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentRepository.sumAmountPaidByLoanAndPaymentType(3L, Payment.PaymentType.CAPITAL))
                .thenReturn(BigDecimal.ZERO);
        when(paymentRepository.sumAmountPaidByLoanAndPaymentType(3L, Payment.PaymentType.INTERES))
                .thenReturn(BigDecimal.ZERO);

        MachineryPayment discountValue = service.applyInterestDiscount(3L, new BigDecimal("50"));

        assertTrue(discountValue.getDiscount());
        assertEquals(Payment.PaymentType.INTERES, discountValue.getPaymentType());
        assertEquals(new BigDecimal("50"), discountValue.getAmountPaid());
    }

    @Test
    void rejectsInvalidDiscountOrAboveBalance() {
        MachineryLoan loan = loan(4L, "1000", LoanStatus.ACTIVO);
        when(loanRepository.findById(4L)).thenReturn(Optional.of(loan));
        assertThrows(IllegalArgumentException.class, () -> service.applyInterestDiscount(4L, BigDecimal.ZERO));

        when(calculationService.calculatePendingInterest(eq(loan), any())).thenReturn(BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class,
                () -> service.applyInterestDiscount(4L, BigDecimal.ONE));

        when(calculationService.calculatePendingInterest(eq(loan), any())).thenReturn(new BigDecimal("10"));
        assertThrows(IllegalArgumentException.class,
                () -> service.applyInterestDiscount(4L, new BigDecimal("11")));
    }

    @Test
    void summarizesPaymentsAndUpdatesLoanToOverdue() {
        MachineryLoan loan = loan(5L, "1000", LoanStatus.ACTIVO);
        when(loanRepository.findById(5L)).thenReturn(Optional.of(loan));
        when(paymentRepository.sumAmountPaidByLoanAndPaymentType(5L, Payment.PaymentType.CAPITAL))
                .thenReturn(new BigDecimal("100"));
        when(paymentRepository.sumAmountPaidByLoanAndPaymentType(5L, Payment.PaymentType.INTERES))
                .thenReturn(new BigDecimal("20"));
        when(calculationService.calculatePendingInterest(eq(loan), any())).thenReturn(new BigDecimal("200"));

        Map<String, BigDecimal> summary = service.computePaymentSummary(5L);

        assertEquals(new BigDecimal("900"), summary.get("capitalPendiente"));
        assertEquals(new BigDecimal("200"), summary.get("interesPendiente"));
        assertEquals(new BigDecimal("220"), summary.get("interesTotal"));
        assertEquals(LoanStatus.VENCIDO, loan.getLoanStatus());
        verify(loanRepository, times(2)).save(loan);
    }

    @Test
    void reopensOverdueLoanAndMarksPaidWhenPrincipalIsComplete() {
        MachineryLoan overdue = loan(6L, "1000", LoanStatus.VENCIDO);
        when(loanRepository.findById(6L)).thenReturn(Optional.of(overdue));
        when(paymentRepository.sumAmountPaidByLoanAndPaymentType(6L, Payment.PaymentType.CAPITAL))
                .thenReturn(new BigDecimal("500"));
        when(paymentRepository.sumAmountPaidByLoanAndPaymentType(6L, Payment.PaymentType.INTERES))
                .thenReturn(null);
        when(calculationService.calculatePendingInterest(eq(overdue), any())).thenReturn(new BigDecimal("-5"));

        Map<String, BigDecimal> reopened = service.computePaymentSummary(6L);
        assertEquals(BigDecimal.ZERO, reopened.get("interesPendiente"));
        assertEquals(LoanStatus.PENDIENTE, overdue.getLoanStatus());

        MachineryLoan paid = loan(7L, "1000", LoanStatus.ACTIVO);
        when(loanRepository.findById(7L)).thenReturn(Optional.of(paid));
        when(paymentRepository.sumAmountPaidByLoanAndPaymentType(7L, Payment.PaymentType.CAPITAL))
                .thenReturn(new BigDecimal("1000"));
        when(paymentRepository.sumAmountPaidByLoanAndPaymentType(7L, Payment.PaymentType.INTERES))
                .thenReturn(BigDecimal.ZERO);
        when(calculationService.calculatePendingInterest(eq(paid), any())).thenReturn(BigDecimal.ZERO);

        service.computePaymentSummary(7L);
        assertEquals(LoanStatus.PAGADO, paid.getLoanStatus());
        verify(loanRepository, times(3)).save(paid);
    }

    @Test
    void exposesPaymentListsAndRejectsUnknownLoanOnSummary() {
        when(paymentRepository.findByMachineryLoanMachineryLoanIdOrderByPaymentDateDesc(8L))
                .thenReturn(List.of());
        when(paymentRepository.findAllPaymentsWithDetails()).thenReturn(List.of());
        assertTrue(service.getPaymentsByMachineryLoan(8L).isEmpty());
        assertTrue(service.getAllPayments().isEmpty());
        assertThrows(RuntimeException.class, () -> service.computePaymentSummary(8L));
    }

    private MachineryLoan loan(Long id, String amount, LoanStatus status) {
        MachineryLoan loan = new MachineryLoan();
        loan.setMachineryLoanId(id);
        loan.setLoanAmount(new BigDecimal(amount));
        loan.setInterestRate(new BigDecimal("5"));
        loan.setLoanStatus(status);
        return loan;
    }

    private MachineryPayment payment(MachineryLoan loan, String amount, Payment.PaymentType type) {
        MachineryPayment payment = new MachineryPayment();
        payment.setMachineryLoan(loan);
        payment.setAmountPaid(new BigDecimal(amount));
        payment.setPaymentType(type);
        return payment;
    }
}
