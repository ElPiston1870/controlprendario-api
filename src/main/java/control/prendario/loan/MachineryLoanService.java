package control.prendario.loan;

import control.prendario.client.Client;
import control.prendario.client.ClientService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MachineryLoanService {

    @Autowired
    private MachineryLoanRepository machineryLoanRepository;

    @Autowired
    private ClientService clientService;

    @Transactional
    public MachineryLoan createMachineryLoan(MachineryLoanDTO loan) {

        Client client = clientService.getActiveById(loan.getClientId());

        // Crear el préstamo
        MachineryLoan machineryLoan = new MachineryLoan();
        machineryLoan.setClient(client);
        machineryLoan.setMachineType(loan.getMachineType());
        machineryLoan.setMachineBrand(loan.getMachineBrand());
        machineryLoan.setLoanAmount(loan.getLoanAmount());
        machineryLoan.setInterestRate(loan.getInterestRate()!= null ? loan.getInterestRate() : new java.math.BigDecimal("5.0")) ;
        machineryLoan.setNotes(loan.getNotes());
        machineryLoan.setLoanStatus(loan.getLoanStatus() != null ?
                loan.getLoanStatus() :
                LoanStatus.ACTIVO);

        return machineryLoanRepository.save(machineryLoan);
    }

    public List<MachineryLoan> getAllMachineryLoans() {
        return machineryLoanRepository.findAllByOrderByMachineryLoanIdDesc().stream()
                .filter(loan -> !Boolean.FALSE.equals(loan.getActive()))
                .toList();
    }

    public MachineryLoan getMachineryLoanById(Long id) {
        return machineryLoanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Préstamo de máquina no encontrado"));
    }

    @Transactional
    public MachineryLoan updateMachineryLoan(Long id, MachineryLoanDTO loan) {
        MachineryLoan existingLoan = machineryLoanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Préstamo de máquina no encontrado"));

        // Actualizar campos permitidos
        if (loan.getLoanAmount() != null) {
            existingLoan.setLoanAmount(loan.getLoanAmount());
        }
        if (loan.getInterestRate() != null) {
            existingLoan.setInterestRate(loan.getInterestRate());
        }
        if (loan.getNotes() != null) {
            existingLoan.setNotes(loan.getNotes());
        }
        if (loan.getLoanStatus() != null) {
            existingLoan.setLoanStatus(loan.getLoanStatus());
        }
        if (loan.getMachineType() != null) {
            existingLoan.setMachineType(loan.getMachineType());
        }
        if(loan.getMachineBrand() != null){
            existingLoan.setMachineBrand((loan.getMachineBrand()));
        }

        // Actualizar cliente si se proporciona
        if (loan.getClientId() != null ) {
            Client client = clientService.getById(loan.getClientId());
            existingLoan.setClient(client);
        }

        return machineryLoanRepository.save(existingLoan);
    }

    public void deleteMachineryLoan(Long id) {
        MachineryLoan loan = getMachineryLoanById(id);
        loan.setActive(false);
        machineryLoanRepository.save(loan);
    }

    public List<MachineryLoan> searchByClient(String term) {
        return machineryLoanRepository.searchByClient(term).stream()
                .filter(loan -> !Boolean.FALSE.equals(loan.getActive()))
                .toList();
    }

    public List<MachineryLoan> searchByTerm(String term) {
        return machineryLoanRepository.findByGeneralSearch(term).stream()
                .filter(loan -> !Boolean.FALSE.equals(loan.getActive()))
                .toList();
    }

    public List<MachineryLoan> searchByFilters(String term, String documentNumber) {
        if (term != null && !term.isEmpty()) {
            return machineryLoanRepository.findByClientFirstNamesContainingIgnoreCaseOrClientLastNamesContainingIgnoreCase(term, term)
                    .stream().filter(loan -> !Boolean.FALSE.equals(loan.getActive())).toList();
        } else if (documentNumber != null && !documentNumber.isEmpty()) {
            return machineryLoanRepository.findByClientDocumentNumberContaining(documentNumber).stream()
                    .filter(loan -> !Boolean.FALSE.equals(loan.getActive())).toList();
        }
        return getAllMachineryLoans();
    }

    public List<MachineryLoan> getOverdueLoans() {
        LocalDateTime now = LocalDateTime.now();
        return machineryLoanRepository.findAll().stream()
                .filter(loan ->
                        loan.getLoanStatus() == LoanStatus.ACTIVO &&
                                loan.getDueDate() != null &&
                                loan.getDueDate().isBefore(now.toLocalDate()))
                .toList();
    }

    public List<MachineryLoan> findOverdueLoans() {
        return machineryLoanRepository.findByLoanStatus(LoanStatus.VENCIDO).stream()
                .filter(loan -> !Boolean.FALSE.equals(loan.getActive()))
                .toList();
    }

    public Long getLastLoanId() {
        return machineryLoanRepository.findMaxId();
    }
}