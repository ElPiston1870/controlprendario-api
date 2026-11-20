package control.prendario.payment.repository;

import control.prendario.payment.domain.MachineryPayment;
import control.prendario.payment.domain.Payment;
import control.prendario.payment.domain.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface MachineryPaymentRepository extends JpaRepository<MachineryPayment, Long> {
    List<MachineryPayment> findByMachineryLoanMachineryLoanIdOrderByPaymentDateDesc(Long machineryLoanId);
    List<MachineryPayment> findByMachineryLoanClientClientIdOrderByPaymentDateDesc(Long clientId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM MachineryPayment p WHERE p.machineryPaymentId = :paymentId")
    Optional<MachineryPayment> findByIdForUpdate(@Param("paymentId") Long paymentId);

    @Query("SELECT SUM(p.amountPaid) FROM MachineryPayment p " +
            "WHERE p.machineryLoan.machineryLoanId = :machineryLoanId " +
            "AND p.paymentType = :paymentType " +
            "AND (p.paymentStatus IS NULL OR p.paymentStatus <> control.prendario.payment.domain.PaymentStatus.CORREGIDO)")
    BigDecimal sumAmountPaidByLoanAndPaymentType(Long machineryLoanId, Payment.PaymentType paymentType);

    @Query("SELECT COALESCE(SUM(p.amountPaid), 0) FROM MachineryPayment p WHERE p.paymentType = :paymentType " +
            "AND (p.paymentStatus IS NULL OR p.paymentStatus <> control.prendario.payment.domain.PaymentStatus.CORREGIDO) " +
            "AND (p.discount IS NULL OR p.discount = false)")
    BigDecimal sumAmountPaidByPaymentType(Payment.PaymentType paymentType);

    @Query("SELECT DISTINCT p FROM MachineryPayment p " +
            "LEFT JOIN FETCH p.machineryLoan pr " +
            "LEFT JOIN FETCH pr.client " +
            "ORDER BY p.paymentDate DESC")
    List<MachineryPayment> findAllPaymentsWithDetails();

    @Query("SELECT COALESCE(SUM(p.amountPaid), 0) FROM MachineryPayment p " +
            "WHERE (p.paymentStatus IS NULL OR p.paymentStatus <> control.prendario.payment.domain.PaymentStatus.CORREGIDO) " +
            "AND (p.discount IS NULL OR p.discount = false)")
    BigDecimal sumTotalPayments();

}
