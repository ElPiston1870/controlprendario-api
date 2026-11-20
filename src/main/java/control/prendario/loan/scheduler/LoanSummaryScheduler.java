package control.prendario.loan.scheduler;

import control.prendario.loan.domain.Loan;
import control.prendario.loan.domain.MachineryLoan;
import control.prendario.loan.service.LoanService;
import control.prendario.loan.service.MachineryLoanService;
import control.prendario.payment.service.MachineryPaymentService;
import control.prendario.payment.service.PaymentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LoanSummaryScheduler {

    @Autowired
    private LoanService loanService;
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private MachineryLoanService machineryLoanService;
    @Autowired
    private MachineryPaymentService machineryPaymentService;

    @Scheduled(cron = "0 0 1 * * *", zone = "America/Bogota")
    public void runSummary() {

        List<Loan> loans = loanService.getAllLoans();
        List<MachineryLoan> machines = machineryLoanService.getAllMachineryLoans();

        for (Loan loan : loans){
            paymentService.computePaymentSummary(loan.getLoanId());
        }

        for(MachineryLoan machine: machines){
            machineryPaymentService.computePaymentSummary(machine.getMachineryLoanId());
        }

        loans = null;
        machines = null;
    }
}
