package control.prendario.payment.repository;

import control.prendario.payment.domain.Payment;
import control.prendario.payment.domain.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByLoanLoanIdOrderByPaymentDateDesc(Long loanId);
    List<Payment> findByLoanClientClientIdOrderByPaymentDateDesc(Long clientId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.paymentId = :paymentId")
    Optional<Payment> findByIdForUpdate(@Param("paymentId") Long paymentId);

    @Query("SELECT p FROM Payment p WHERE p.loan.client.firstNames LIKE %:term% " +
            "OR p.loan.client.lastNames LIKE %:term% " +
            "OR p.loan.client.documentNumber LIKE %:term%")
    List<Payment> searchByClient(String term);

    @Query("SELECT SUM(p.amountPaid) FROM Payment p WHERE p.loan.loanId = :loanId " +
            "AND p.paymentType = :paymentType AND (p.paymentStatus IS NULL OR p.paymentStatus <> control.prendario.payment.domain.PaymentStatus.CORREGIDO)")
    BigDecimal sumAmountPaidByLoanAndPaymentType(Long loanId, Payment.PaymentType paymentType);

    @Query("SELECT COALESCE(SUM(p.amountPaid), 0) FROM Payment p WHERE p.paymentType = :paymentType " +
            "AND (p.paymentStatus IS NULL OR p.paymentStatus <> control.prendario.payment.domain.PaymentStatus.CORREGIDO) " +
            "AND (p.discount IS NULL OR p.discount = false)")
    BigDecimal sumAmountPaidByPaymentType(Payment.PaymentType paymentType);

    @Query("SELECT DISTINCT p FROM Payment p " +
            "LEFT JOIN FETCH p.loan pr " +
            "LEFT JOIN FETCH pr.client " +
            "ORDER BY p.paymentDate DESC")
    List<Payment> findAllPaymentsWithDetails();

    @Query("SELECT COALESCE(SUM(p.amountPaid), 0) FROM Payment p " +
            "WHERE (p.paymentStatus IS NULL OR p.paymentStatus <> control.prendario.payment.domain.PaymentStatus.CORREGIDO) " +
            "AND (p.discount IS NULL OR p.discount = false)")
    BigDecimal sumTotalPayments();

    @Query("SELECT p FROM Payment p " +
            "WHERE p.loan.loanId = :loanId " +
            "AND p.paymentType = :paymentType")
    List<Payment> findByLoanAndPaymentType(
            @Param("loanId") Long loanId,
            @Param("paymentType") Payment.PaymentType paymentType
    );
}
