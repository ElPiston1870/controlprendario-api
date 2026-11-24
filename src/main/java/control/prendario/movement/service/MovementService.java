package control.prendario.movement.service;

import control.prendario.shared.query.BrowseQuery;
import control.prendario.shared.query.CatalogFilters;
import control.prendario.shared.query.PageResult;
import org.springframework.data.domain.Sort;
import control.prendario.movement.domain.Movement;
import control.prendario.movement.repository.MovementRepository;
import control.prendario.loan.repository.LoanRepository;
import control.prendario.loan.domain.LoanStatus;
import control.prendario.loan.repository.MachineryLoanRepository;
import control.prendario.payment.repository.MachineryPaymentRepository;
import control.prendario.payment.domain.Payment;
import control.prendario.payment.repository.PaymentRepository;

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

    public PageResult<Movement> pageMovements(BrowseQuery q) {
        return PageResult.from(movementRepository.findAll(CatalogFilters.movements(q),
                q.page(Sort.by(Sort.Direction.DESC, "movementDate", "movementId"))));
    }
}
