package control.prendario.collateral;

import control.prendario.loan.Loan;
import control.prendario.loan.LoanRepository;
import control.prendario.loan.LoanStatus;
import control.prendario.loan.MachineryLoan;
import control.prendario.loan.MachineryLoanRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CollateralInventoryService {

    private final LoanRepository loanRepository;
    private final MachineryLoanRepository machineryLoanRepository;

    @Transactional(readOnly = true)
    public List<CollateralInventoryDTO> getInventory() {
        List<CollateralInventoryDTO> inventoryItems = new ArrayList<>();

        for (Loan loan : loanRepository.findAll()) {
            if (Boolean.FALSE.equals(loan.getActive())) {
                continue;
            }
            String collateral = loan.getVehicle() == null
                    ? "Sin vehículo asociado"
                    : String.format("%s %s %s %s",
                    value(loan.getVehicle().getVehicleType()),
                    value(loan.getVehicle().getBrand()),
                    value(loan.getVehicle().getLine()),
                    value(loan.getVehicle().getModel()));
            String plate = loan.getVehicle() == null
                    ? "No registrada" : value(loan.getVehicle().getPlate());
            inventoryItems.add(CollateralInventoryDTO.builder()
                    .collateralType("VEHICULO")
                    .loanId(loan.getLoanId())
                    .clientId(loan.getClient() == null ? null : loan.getClient().getClientId())
                    .clientName(clientName(loan))
                    .identifier(plate)
                    .description(collateral)
                    .loanAmount(loan.getLoanAmount())
                    .pendingPrincipal(amountOrFallback(loan.getPendingPrincipal(), loan.getLoanAmount()))
                    .loanStatus(value(loan.getLoanStatus()))
                    .loanDate(loan.getLoanDate())
                    .dueDate(value(loan.getDueDate()))
                    .inCollateral(!isLoanSettled(loan.getLoanStatus()))
                    .build());
        }

        for (MachineryLoan loan : machineryLoanRepository.findAll()) {
            if (Boolean.FALSE.equals(loan.getActive())) {
                continue;
            }
            inventoryItems.add(CollateralInventoryDTO.builder()
                    .collateralType("MAQUINARIA")
                    .loanId(loan.getMachineryLoanId())
                    .clientId(loan.getClient() == null ? null : loan.getClient().getClientId())
                    .clientName(clientName(loan))
                    .identifier(value(loan.getMachineType()) + " " + value(loan.getMachineBrand()))
                    .description("Maquinaria en garantía")
                    .loanAmount(loan.getLoanAmount())
                    .pendingPrincipal(amountOrFallback(loan.getPendingPrincipal(), loan.getLoanAmount()))
                    .loanStatus(value(loan.getLoanStatus()))
                    .loanDate(loan.getLoanDate())
                    .dueDate(value(loan.getDueDate()))
                    .inCollateral(!isLoanSettled(loan.getLoanStatus()))
                    .build());
        }

        return inventoryItems;
    }

    private String clientName(Loan loan) {
        if (loan.getClient() == null) {
            return "Cliente no registrado";
        }
        return value(loan.getClient().getFirstNames()) + " " + value(loan.getClient().getLastNames());
    }

    private String clientName(MachineryLoan loan) {
        if (loan.getClient() == null) {
            return "Cliente no registrado";
        }
        return value(loan.getClient().getFirstNames()) + " " + value(loan.getClient().getLastNames());
    }

    private boolean isLoanSettled(LoanStatus status) {
        return status == LoanStatus.PAGADO;
    }

    private BigDecimal amountOrFallback(BigDecimal amount, BigDecimal fallback) {
        return amount != null ? amount : (fallback != null ? fallback : BigDecimal.ZERO);
    }

    private String value(Object value) {
        return value == null || value.toString().isBlank() ? "No registrado" : value.toString();
    }
}
