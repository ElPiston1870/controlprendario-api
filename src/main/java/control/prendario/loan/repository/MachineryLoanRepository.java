package control.prendario.loan.repository;

import control.prendario.loan.domain.LoanStatus;
import control.prendario.loan.domain.MachineryLoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MachineryLoanRepository extends JpaRepository<MachineryLoan, Long> {

    @Query("SELECT p FROM MachineryLoan p JOIN p.client c " +
            "WHERE LOWER(c.firstNames) LIKE LOWER(CONCAT('%', :term, '%')) " +
            "OR LOWER(c.lastNames) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<MachineryLoan> searchByClient(String term);

    @Query("SELECT p FROM MachineryLoan p JOIN p.client c " +
            "WHERE LOWER(c.firstNames) LIKE LOWER(CONCAT('%', :term, '%')) " +
            "OR LOWER(c.lastNames) LIKE LOWER(CONCAT('%', :term, '%')) " +
            "OR LOWER(c.documentNumber) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<MachineryLoan> findByGeneralSearch(String term);

    @Query("SELECT MAX(p.machineryLoanId) FROM MachineryLoan p")
    Long findMaxId();

    @Query("SELECT COALESCE(SUM(p.loanAmount), 0) FROM MachineryLoan p")
    BigDecimal sumTotalLoans();

    @Query("SELECT COALESCE(SUM(p.loanAmount), 0) FROM MachineryLoan p WHERE p.loanStatus IN (:statuses)")
    BigDecimal sumAmountsByStatus(@Param("statuses") LoanStatus... statuses);

    List<MachineryLoan> findByLoanStatus(LoanStatus loanStatus);
    List<MachineryLoan> findByClientFirstNamesContainingIgnoreCaseOrClientLastNamesContainingIgnoreCase(String firstNames, String lastNames);
    List<MachineryLoan> findByClientDocumentNumberContaining(String documentNumber);
    List<MachineryLoan> findAllByOrderByMachineryLoanIdDesc();
    List<MachineryLoan> findByClientClientIdOrderByLoanDateDesc(Long clientId);

}
