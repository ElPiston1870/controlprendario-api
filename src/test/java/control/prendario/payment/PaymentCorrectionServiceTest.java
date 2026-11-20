package control.prendario.payment;

import control.prendario.loan.Loan;
import control.prendario.loan.LoanRepository;
import control.prendario.loan.LoanStatus;
import control.prendario.loan.MachineryLoan;
import control.prendario.loan.MachineryLoanRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentCorrectionServiceTest {

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
    void correctsVehiclePaymentKeepingOriginalAndUpdatingSummary() {
        Loan loan = buildLoan();
        Payment original = new Payment();
        original.setPaymentId(5L);
        original.setLoan(loan);
        original.setPaymentDate(LocalDateTime.of(2026, 1, 10, 9, 0));
        original.setAmountPaid(new BigDecimal("100"));
        original.setPaymentType(Payment.PaymentType.CAPITAL);

        when(paymentRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(original));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            if (payment.getPaymentId() == null) {
                payment.setPaymentId(6L);
            }
            return payment;
        });
        when(loanRepository.findById(2L)).thenReturn(Optional.of(loan));
        when(paymentRepository.sumAmountPaidByLoanAndPaymentType(2L, Payment.PaymentType.CAPITAL))
                .thenReturn(new BigDecimal("250"));
        when(paymentRepository.sumAmountPaidByLoanAndPaymentType(2L, Payment.PaymentType.INTERES))
                .thenReturn(BigDecimal.ZERO);
        when(interestCalculationService.calculatePendingInterest(any(), any())).thenReturn(BigDecimal.ZERO);

        Payment replacement = paymentService.correctPayment(5L, buildCorrection("250"));

        assertEquals(PaymentStatus.CORREGIDO, original.getPaymentStatus());
        assertEquals("Error de digitación", original.getCorrectionReason());
        assertEquals(6L, original.getCorrectedByPaymentId());
        assertEquals(5L, replacement.getReplacesPaymentId());
        assertEquals(new BigDecimal("250"), replacement.getAmountPaid());
        assertEquals(original.getPaymentDate(), replacement.getPaymentDate());
        verify(paymentRepository).save(original);
    }

    @Test
    void doesNotAllowCorrectingAlreadyCorrectedPayment() {
        Payment original = new Payment();
        original.setPaymentStatus(PaymentStatus.CORREGIDO);
        when(paymentRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(original));

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.correctPayment(5L, buildCorrection("250")));
    }

    @Test
    void correctsMachineryPaymentKeepingOriginal() {
        MachineryLoan loan = new MachineryLoan();
        loan.setMachineryLoanId(3L);
        loan.setLoanAmount(new BigDecimal("10000"));
        loan.setLoanStatus(LoanStatus.ACTIVO);

        MachineryPayment original = new MachineryPayment();
        original.setMachineryPaymentId(8L);
        original.setMachineryLoan(loan);
        original.setPaymentDate(LocalDateTime.of(2026, 1, 10, 9, 0));
        original.setAmountPaid(new BigDecimal("100"));
        original.setPaymentType(Payment.PaymentType.CAPITAL);

        when(machineryPaymentRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(original));
        when(machineryPaymentRepository.save(any(MachineryPayment.class))).thenAnswer(invocation -> {
            MachineryPayment payment = invocation.getArgument(0);
            if (payment.getMachineryPaymentId() == null) {
                payment.setMachineryPaymentId(9L);
            }
            return payment;
        });
        when(machineryLoanRepository.findById(3L)).thenReturn(Optional.of(loan));
        when(machineryPaymentRepository.sumAmountPaidByLoanAndPaymentType(3L, Payment.PaymentType.CAPITAL))
                .thenReturn(new BigDecimal("250"));
        when(machineryPaymentRepository.sumAmountPaidByLoanAndPaymentType(3L, Payment.PaymentType.INTERES))
                .thenReturn(BigDecimal.ZERO);
        when(machineryInterestCalculationService.calculatePendingInterest(any(), any())).thenReturn(BigDecimal.ZERO);

        MachineryPayment replacement = machineryPaymentService.correctPayment(8L, buildCorrection("250"));

        assertEquals(PaymentStatus.CORREGIDO, original.getPaymentStatus());
        assertEquals(9L, original.getCorrectedByPaymentId());
        assertEquals(8L, replacement.getReplacesPaymentId());
        assertEquals(new BigDecimal("250"), replacement.getAmountPaid());
        verify(machineryPaymentRepository).save(original);
    }

    private Loan buildLoan() {
        Loan loan = new Loan();
        loan.setLoanId(2L);
        loan.setLoanAmount(new BigDecimal("10000"));
        loan.setLoanStatus(LoanStatus.ACTIVO);
        return loan;
    }

    private PaymentCorrectionDTO buildCorrection(String amount) {
        PaymentCorrectionDTO correction = new PaymentCorrectionDTO();
        correction.setAmountPaid(new BigDecimal(amount));
        correction.setPaymentType(Payment.PaymentType.CAPITAL);
        correction.setPaymentMethod(Payment.PaymentMethod.EFECTIVO);
        correction.setReason("Error de digitación");
        return correction;
    }
}
