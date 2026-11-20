package control.prendario.payment.service;

import control.prendario.payment.domain.MachineryPayment;
import control.prendario.payment.domain.Payment;
import control.prendario.payment.domain.PaymentStatus;
import control.prendario.payment.repository.MachineryPaymentRepository;
import control.prendario.loan.domain.MachineryLoan;

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
class MachineryInterestCalculationServiceTest {

    @Mock
    private MachineryPaymentRepository paymentRepository;

    @InjectMocks
    private MachineryInterestCalculationService service;

    @Test
    void calculatesMachineryInterestSubtractingApplicablePayments() {
        MachineryLoan loan = new MachineryLoan();
        loan.setMachineryLoanId(3L);
        loan.setLoanAmount(new BigDecimal("1000"));
        loan.setInterestRate(new BigDecimal("10"));
        loan.setLoanDate(LocalDateTime.of(2025, 1, 1, 0, 0));

        MachineryPayment capital = payment(Payment.PaymentType.CAPITAL, "200", LocalDate.of(2025, 2, 1), PaymentStatus.ACTIVO);
        MachineryPayment interest = payment(Payment.PaymentType.INTERES, "30", LocalDate.of(2025, 2, 1), PaymentStatus.ACTIVO);
        MachineryPayment corrected = payment(Payment.PaymentType.CAPITAL, "900", LocalDate.of(2025, 1, 15), PaymentStatus.CORREGIDO);
        when(paymentRepository.findByMachineryLoanMachineryLoanIdOrderByPaymentDateDesc(3L))
                .thenReturn(List.of(capital, interest, corrected));

        assertEquals(new BigDecimal("150"),
                service.calculatePendingInterest(loan, LocalDate.of(2025, 2, 1)));
    }

    @Test
    void rejectsDateBeforeLoan() {
        MachineryLoan loan = new MachineryLoan();
        loan.setLoanDate(LocalDateTime.of(2025, 1, 2, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> service.calculatePendingInterest(loan, LocalDate.of(2025, 1, 1)));
    }

    private MachineryPayment payment(Payment.PaymentType type, String amount, LocalDate date, PaymentStatus status) {
        MachineryPayment payment = new MachineryPayment();
        payment.setPaymentType(type);
        payment.setAmountPaid(new BigDecimal(amount));
        payment.setPaymentDate(date.atStartOfDay());
        payment.setPaymentStatus(status);
        return payment;
    }
}
