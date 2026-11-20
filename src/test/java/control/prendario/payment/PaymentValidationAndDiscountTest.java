package control.prendario.payment;

import control.prendario.loan.Loan;
import control.prendario.loan.LoanRepository;
import control.prendario.loan.MachineryLoan;
import control.prendario.loan.MachineryLoanRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Validación de pagos (HU-04) y marca de descuento (HU-07, RN-14).
 */
@ExtendWith(MockitoExtension.class)
class PaymentValidationAndDiscountTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private InterestCalculationService interestCalculationService;
    @Mock
    private MachineryPaymentRepository machineryPaymentRepository;
    @Mock
    private MachineryLoanRepository machineryLoanRepository;
    @Mock
    private MachineryInterestCalculationService machineryInterestCalculationService;

    @InjectMocks
    private PaymentService paymentService;
    @InjectMocks
    private MachineryPaymentService machineryPaymentService;

    @Test
    void rejectsPaymentWithZeroOrNegativeAmount() {
        Payment payment = vehiclePayment(BigDecimal.ZERO, Payment.PaymentType.CAPITAL);
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> paymentService.createPayment(payment));
        assertEquals("El monto del pago debe ser mayor que cero", error.getMessage());

        Payment negative = vehiclePayment(new BigDecimal("-1"), Payment.PaymentType.CAPITAL);
        assertThrows(IllegalArgumentException.class, () -> paymentService.createPayment(negative));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void rejectsPaymentWithoutTypeOrLoan() {
        assertThrows(IllegalArgumentException.class,
                () -> paymentService.createPayment(vehiclePayment(new BigDecimal("1000"), null)));
        Payment withoutLoan = new Payment();
        withoutLoan.setAmountPaid(new BigDecimal("1000"));
        withoutLoan.setPaymentType(Payment.PaymentType.CAPITAL);
        assertThrows(IllegalArgumentException.class, () -> paymentService.createPayment(withoutLoan));
    }

    @Test
    void rejectsMachineryPaymentWithInvalidAmount() {
        MachineryPayment payment = new MachineryPayment();
        MachineryLoan loan = new MachineryLoan();
        loan.setMachineryLoanId(3L);
        payment.setMachineryLoan(loan);
        payment.setPaymentType(Payment.PaymentType.INTERES);
        payment.setAmountPaid(null);
        assertThrows(IllegalArgumentException.class, () -> machineryPaymentService.createPayment(payment));
        verify(machineryPaymentRepository, never()).save(any());
    }

    @Test
    void regularPaymentIsNotFlaggedAsDiscountEvenIfClientSendsIt() {
        Loan loan = loan();
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment payment = vehiclePayment(new BigDecimal("50000"), Payment.PaymentType.INTERES);
        payment.setDiscount(true);
        Payment saved = paymentService.createPayment(payment);

        assertFalse(saved.getDiscount());
    }

    @Test
    void vehicleDiscountIsFlaggedToExcludeItFromBalance() {
        Loan loan = loan();
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(interestCalculationService.calculatePendingInterest(any(), any())).thenReturn(new BigDecimal("40000"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment discountValue = paymentService.applyInterestDiscount(1L, new BigDecimal("10000"));

        assertTrue(discountValue.getDiscount());
        assertEquals(Payment.PaymentType.INTERES, discountValue.getPaymentType());
        assertEquals(new BigDecimal("10000"), discountValue.getAmountPaid());
    }

    @Test
    void machineryDiscountIsFlagged() {
        MachineryLoan loan = new MachineryLoan();
        loan.setMachineryLoanId(3L);
        loan.setLoanAmount(new BigDecimal("1000000"));
        loan.setInterestRate(new BigDecimal("5"));
        loan.setLoanDate(LocalDateTime.now().minusDays(40));
        when(machineryLoanRepository.findById(3L)).thenReturn(Optional.of(loan));
        when(machineryInterestCalculationService.calculatePendingInterest(any(), any())).thenReturn(new BigDecimal("40000"));
        ArgumentCaptor<MachineryPayment> captor = ArgumentCaptor.forClass(MachineryPayment.class);
        when(machineryPaymentRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        machineryPaymentService.applyInterestDiscount(3L, new BigDecimal("10000"));

        assertTrue(captor.getAllValues().get(0).getDiscount());
    }

    private Payment vehiclePayment(BigDecimal amount, Payment.PaymentType type) {
        Payment payment = new Payment();
        Loan loan = new Loan();
        loan.setLoanId(1L);
        payment.setLoan(loan);
        payment.setAmountPaid(amount);
        payment.setPaymentType(type);
        payment.setPaymentMethod(Payment.PaymentMethod.EFECTIVO);
        return payment;
    }

    private Loan loan() {
        Loan loan = new Loan();
        loan.setLoanId(1L);
        loan.setLoanAmount(new BigDecimal("1000000"));
        loan.setInterestRate(new BigDecimal("5"));
        loan.setLoanDate(LocalDateTime.now().minusDays(40));
        return loan;
    }
}
