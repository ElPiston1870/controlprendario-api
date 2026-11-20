package control.prendario.loan.repository;

import control.prendario.loan.domain.Loan;
import control.prendario.loan.domain.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query("SELECT p FROM Loan p JOIN p.client c " +
            "WHERE LOWER(c.firstNames) LIKE LOWER(CONCAT('%', :term, '%')) " +
            "OR LOWER(c.lastNames) LIKE LOWER(CONCAT('%', :term, '%')) " +
            "OR LOWER(c.documentNumber) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<Loan> findByGeneralSearch(String term);

    @Query("SELECT COALESCE(SUM(p.loanAmount), 0) FROM Loan p")
    BigDecimal sumTotalLoans();

    @Query("SELECT COALESCE(SUM(p.loanAmount), 0) FROM Loan p WHERE p.loanStatus IN (:statuses)")
    BigDecimal sumAmountsByStatus(@Param("statuses") LoanStatus... statuses);

    // Consulta personalizada para obtener el ID más alto
    @Query("SELECT MAX(p.loanId) FROM Loan p")
    Long findMaxId();

    List<Loan> findByLoanStatus(LoanStatus loanStatus);
    List<Loan> findAllByOrderByLoanIdDesc();
    List<Loan> findByClientFirstNamesContainingIgnoreCaseOrClientLastNamesContainingIgnoreCase(String firstNames, String lastNames);
    List<Loan> findByClientDocumentNumberContaining(String documentNumber);
    List<Loan> findByClientClientIdOrderByLoanDateDesc(Long clientId);
}
