package control.prendario.movement.service;

import control.prendario.movement.domain.Movement;
import control.prendario.movement.repository.MovementRepository;
import control.prendario.loan.repository.LoanRepository;
import control.prendario.loan.domain.LoanStatus;
import control.prendario.loan.repository.MachineryLoanRepository;
import control.prendario.payment.repository.MachineryPaymentRepository;
import control.prendario.payment.domain.Payment;
import control.prendario.payment.repository.PaymentRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovementServiceTest {

    @Mock private MovementRepository movementRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private MachineryPaymentRepository machineryPaymentRepository;
    @Mock private LoanRepository loanRepository;
    @Mock private MachineryLoanRepository machineryLoanRepository;
    @InjectMocks private MovementService service;

    @Test
    void registersAndListsMovements() {
        Movement movement = new Movement();
        when(movementRepository.save(movement)).thenReturn(movement);
        when(movementRepository.findAllByOrderByMovementDateDesc()).thenReturn(List.of(movement));

        assertEquals(movement, service.createMovement(movement));
        assertEquals(List.of(movement), service.getAllMovements());
    }

    @Test
    void cp15CalculatesAvailabilityWithReferenceInflowsOutflowsLoansAndPayments() {
        when(paymentRepository.sumAmountPaidByPaymentType(Payment.PaymentType.INTERES)).thenReturn(new BigDecimal("150000"));
        when(machineryPaymentRepository.sumAmountPaidByPaymentType(Payment.PaymentType.INTERES)).thenReturn(BigDecimal.ZERO);
        when(movementRepository.calculateBalance()).thenReturn(new BigDecimal("1900000"));
        when(paymentRepository.sumTotalPayments()).thenReturn(new BigDecimal("150000"));
        when(machineryPaymentRepository.sumTotalPayments()).thenReturn(BigDecimal.ZERO);
        when(loanRepository.sumTotalLoans()).thenReturn(new BigDecimal("1000000"));
        when(machineryLoanRepository.sumTotalLoans()).thenReturn(BigDecimal.ZERO);
        when(loanRepository.sumAmountsByStatus(
                LoanStatus.ACTIVO, LoanStatus.VENCIDO, LoanStatus.PENDIENTE))
                .thenReturn(new BigDecimal("1000000"));
        when(machineryLoanRepository.sumAmountsByStatus(
                LoanStatus.ACTIVO, LoanStatus.VENCIDO, LoanStatus.PENDIENTE))
                .thenReturn(BigDecimal.ZERO);

        Map<String, BigDecimal> balance = service.getFullBalance();

        assertEquals(new BigDecimal("1900000"), balance.get("movimientos"));
        assertEquals(new BigDecimal("150000"), balance.get("pagos"));
        assertEquals(new BigDecimal("150000"), balance.get("interes"));
        assertEquals(new BigDecimal("1000000"), balance.get("prestamos"));
        assertEquals(new BigDecimal("1000000"), balance.get("activos"));
        assertEquals(new BigDecimal("1050000"), balance.get("total"));
    }
}
