package control.prendario.movement;

import control.prendario.loan.LoanRepository;
import control.prendario.loan.LoanStatus;
import control.prendario.loan.MachineryLoanRepository;
import control.prendario.payment.MachineryPaymentRepository;
import control.prendario.payment.Payment;
import control.prendario.payment.PaymentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MovementService {

    @Autowired
    private MovementRepository movementRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private MachineryPaymentRepository machineryPaymentRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private MachineryLoanRepository machineryLoanRepository;

    public Movement createMovement(Movement movement) {
        return movementRepository.save(movement);
    }

    public List<Movement> getAllMovements() {
        return movementRepository.findAllByOrderByMovementDateDesc();
    }

    public Map<String, BigDecimal> getFullBalance() {
        Map<String, BigDecimal> balance = new HashMap<>();

        BigDecimal totalInterestPaid = paymentRepository.sumAmountPaidByPaymentType(Payment.PaymentType.INTERES).add(machineryPaymentRepository.sumAmountPaidByPaymentType(Payment.PaymentType.INTERES));

        // Obtener balance de movimientos
        BigDecimal movementsBalance = movementRepository.calculateBalance();

        // Obtener total de pagos
        BigDecimal totalPayments = paymentRepository.sumTotalPayments().add(machineryPaymentRepository.sumTotalPayments());
        if (totalPayments == null) totalPayments = BigDecimal.ZERO;

        // Obtener total de préstamos
        BigDecimal totalLoans = loanRepository.sumTotalLoans().add(machineryLoanRepository.sumTotalLoans());
        if (totalLoans == null) totalLoans = BigDecimal.ZERO;

        //Obtener total de prestamos activos
        BigDecimal activeLoans = loanRepository.sumAmountsByStatus(LoanStatus.ACTIVO, LoanStatus.VENCIDO, LoanStatus.PENDIENTE).add(machineryLoanRepository.sumAmountsByStatus(LoanStatus.ACTIVO, LoanStatus.VENCIDO, LoanStatus.PENDIENTE));

        // Calcular balance final
        BigDecimal totalBalance = movementsBalance
                .add(totalPayments)
                .subtract(totalLoans);

        balance.put("movimientos", movementsBalance);
        balance.put("pagos", totalPayments);
        balance.put("interes", totalInterestPaid);
        balance.put("prestamos", totalLoans);
        balance.put("activos", activeLoans);
        balance.put("total", totalBalance);

        return balance;
    }
}
