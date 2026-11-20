package control.prendario.payment;

import control.prendario.loan.Loan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InterestCalculationService {

    @Autowired
    private PaymentRepository paymentRepository;

    public BigDecimal calculatePendingInterest(Loan loan, LocalDate currentDate) {

        BigDecimal loanInterest = loan.getInterestRate();
        BigDecimal currentPrincipal = loan.getLoanAmount();
        LocalDate loanStartDate = loan.getLoanDate().toLocalDate();
        int numberOfMonths = calculateMonthsBetweenDates(loanStartDate, currentDate);

        BigDecimal pendingInterest = BigDecimal.ZERO;
        //obtine los pagos y los filtra segun su tipo
        List<Payment> sortedPayments = paymentRepository.findByLoanLoanIdOrderByPaymentDateDesc(loan.getLoanId());
        List<Payment> principalPayments = sortedPayments.stream()
                .filter(payment -> payment.getPaymentStatus() != PaymentStatus.CORREGIDO)
                .filter(payment -> payment.getPaymentType() == Payment.PaymentType.CAPITAL)
                .toList();
        List<Payment> interestPayments = sortedPayments.stream()
                .filter(payment -> payment.getPaymentStatus() != PaymentStatus.CORREGIDO)
                .filter(payment -> payment.getPaymentType() == Payment.PaymentType.INTERES)
                .toList();

        for(int i = 0; i < numberOfMonths; i++){
            pendingInterest = pendingInterest.add(currentPrincipal.multiply(loanInterest).divide(BigDecimal.valueOf(100)));
            //calcula el capital restante por meses
            if (principalPayments != null && !principalPayments.isEmpty()) {
                for (int j = 0; j < principalPayments.size(); j++) {
                    Payment payment = principalPayments.get(j);
                    if ((payment.getPaymentDate().toLocalDate().isAfter(loanStartDate.plusMonths(i )) || payment.getPaymentDate().toLocalDate().isEqual(loanStartDate.plusMonths(i )) ) &&
                            payment.getPaymentDate().toLocalDate().isBefore(loanStartDate.plusMonths(i + 1))) {
                        currentPrincipal = currentPrincipal.subtract(payment.getAmountPaid());
                    }
                }
            }
            //calcula el interes por mes
            if (interestPayments != null && !interestPayments.isEmpty()) {
                for (int j = 0; j < interestPayments.size(); j++) {
                    Payment payment = interestPayments.get(j);
                    if ((payment.getPaymentDate().toLocalDate().isAfter(loanStartDate.plusMonths(i )) || payment.getPaymentDate().toLocalDate().isEqual(loanStartDate.plusMonths(i )) ) &&
                            payment.getPaymentDate().toLocalDate().isBefore(loanStartDate.plusMonths(i + 1))) {
                        pendingInterest = pendingInterest.subtract(payment.getAmountPaid());
                    }
                }
            }
        }

        return pendingInterest;
    }

    private int calculateMonthsBetweenDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la fecha final");

        }
        // Si las fechas son del mismo mes y año, retorna 0
        if (startDate.getYear() == endDate.getYear() && startDate.getMonth() == endDate.getMonth()) {
            return 1;
        }

        // Calculamos los meses entre estas dos fechas
        return (int) ChronoUnit.MONTHS.between(startDate, endDate) + 1;
    }
}
