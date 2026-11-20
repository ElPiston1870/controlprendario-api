package control.prendario.payment.service;

import control.prendario.payment.domain.Payment;
import control.prendario.payment.domain.PaymentStatus;
import control.prendario.payment.repository.PaymentRepository;
import control.prendario.loan.domain.Loan;

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
class InterestCalculationServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private InterestCalculationService service;

    @Test
    void cp09CalculatesOnePeriodForOneMillionLoanAtFivePercent() {
        Loan loan = referenceLoan();
        when(paymentRepository.findByLoanLoanIdOrderByPaymentDateDesc(22L)).thenReturn(List.of());

        BigDecimal interest = service.calculatePendingInterest(loan, LocalDate.of(2026, 1, 6));

        assertEquals(new BigDecimal("50000"), interest);
    }

    @Test
    void cp10CalculatesTwoPeriodsWithoutPrincipalPayments() {
        Loan loan = referenceLoan();
        when(paymentRepository.findByLoanLoanIdOrderByPaymentDateDesc(22L)).thenReturn(List.of());

        BigDecimal interest = service.calculatePendingInterest(loan, LocalDate.of(2026, 2, 6));

        assertEquals(new BigDecimal("100000"), interest);
    }

    @Test
    void cp11AppliesTwoHundredThousandPrincipalPaymentFromNextPeriod() {
        Loan loan = referenceLoan();
        Payment principalPayment = payment(Payment.PaymentType.CAPITAL, "200000",
                LocalDate.of(2026, 1, 20), PaymentStatus.ACTIVO);
        when(paymentRepository.findByLoanLoanIdOrderByPaymentDateDesc(22L))
                .thenReturn(List.of(principalPayment));

        BigDecimal interest = service.calculatePendingInterest(loan, LocalDate.of(2026, 2, 6));
        BigDecimal pendingPrincipal = loan.getLoanAmount().subtract(principalPayment.getAmountPaid());

        assertEquals(new BigDecimal("800000"), pendingPrincipal);
        assertEquals(new BigDecimal("90000"), interest);
    }

    @Test
    void cp12SubtractsInterestPaymentAndKeepsPendingPrincipal() {
        Loan loan = referenceLoan();
        Payment principalPayment = payment(Payment.PaymentType.CAPITAL, "200000",
                LocalDate.of(2026, 1, 20), PaymentStatus.ACTIVO);
        Payment interestPayment = payment(Payment.PaymentType.INTERES, "50000",
                LocalDate.of(2026, 1, 25), PaymentStatus.ACTIVO);
        when(paymentRepository.findByLoanLoanIdOrderByPaymentDateDesc(22L))
                .thenReturn(List.of(principalPayment, interestPayment));

        BigDecimal pendingInterest = service.calculatePendingInterest(
                loan, LocalDate.of(2026, 2, 6));
        BigDecimal pendingPrincipal = loan.getLoanAmount().subtract(principalPayment.getAmountPaid());

        assertEquals(new BigDecimal("800000"), pendingPrincipal);
        assertEquals(new BigDecimal("40000"), pendingInterest);
    }

    @Test
    void calculatesInterestWithPrincipalAndInterestPaymentsIgnoringCorrected() {
        Loan loan = new Loan();
        loan.setLoanId(8L);
        loan.setLoanAmount(new BigDecimal("1000"));
        loan.setInterestRate(new BigDecimal("10"));
        loan.setLoanDate(LocalDateTime.of(2025, 1, 15, 10, 0));

        Payment principalPayment = payment(Payment.PaymentType.CAPITAL, "200", LocalDate.of(2025, 2, 15), PaymentStatus.ACTIVO);
        Payment interestPayment = payment(Payment.PaymentType.INTERES, "50", LocalDate.of(2025, 2, 10), PaymentStatus.ACTIVO);
        Payment correctedPayment = payment(Payment.PaymentType.INTERES, "1000", LocalDate.of(2025, 2, 10), PaymentStatus.CORREGIDO);
        when(paymentRepository.findByLoanLoanIdOrderByPaymentDateDesc(8L))
                .thenReturn(List.of(principalPayment, interestPayment, correctedPayment));

        BigDecimal interest = service.calculatePendingInterest(loan, LocalDate.of(2025, 3, 15));

        assertEquals(new BigDecimal("230"), interest);
    }

    @Test
    void calculatesOneMonthWhenLoanAndQueryAreInSameMonth() {
        Loan loan = new Loan();
        loan.setLoanId(9L);
        loan.setLoanAmount(new BigDecimal("500"));
        loan.setInterestRate(new BigDecimal("5"));
        loan.setLoanDate(LocalDateTime.of(2025, 4, 1, 0, 0));
        when(paymentRepository.findByLoanLoanIdOrderByPaymentDateDesc(9L)).thenReturn(List.of());

        assertEquals(new BigDecimal("25"),
                service.calculatePendingInterest(loan, LocalDate.of(2025, 4, 20)));
    }

    @Test
    void rejectsDateBeforeLoanStart() {
        Loan loan = new Loan();
        loan.setLoanId(10L);
        loan.setLoanAmount(BigDecimal.ONE);
        loan.setInterestRate(BigDecimal.ONE);
        loan.setLoanDate(LocalDateTime.of(2025, 4, 2, 0, 0));

        assertThrows(IllegalArgumentException.class,
                () -> service.calculatePendingInterest(loan, LocalDate.of(2025, 4, 1)));
    }

    private Loan referenceLoan() {
        Loan loan = new Loan();
        loan.setLoanId(22L);
        loan.setLoanAmount(new BigDecimal("1000000"));
        loan.setInterestRate(new BigDecimal("5"));
        loan.setLoanDate(LocalDateTime.of(2026, 1, 5, 0, 0));
        return loan;
    }

    private Payment payment(Payment.PaymentType type, String amount, LocalDate date, PaymentStatus status) {
        Payment payment = new Payment();
        payment.setPaymentType(type);
        payment.setAmountPaid(new BigDecimal(amount));
        payment.setPaymentDate(date.atStartOfDay());
        payment.setPaymentStatus(status);
        return payment;
    }
}
