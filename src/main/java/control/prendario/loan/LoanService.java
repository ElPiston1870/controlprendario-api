package control.prendario.loan;

import control.prendario.client.Client;
import control.prendario.client.ClientService;
import control.prendario.vehicle.Vehicle;
import control.prendario.vehicle.VehicleDTO;
import control.prendario.vehicle.VehicleService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LoanService {
    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private ClientService clientService;

    @Autowired
    private VehicleService vehicleService;

    @Transactional
    public Loan saveLoan(LoanDTO loanDTO) {
        // Obtener el cliente
        Client client = clientService.getActiveById(loanDTO.getClientId());

        // Buscar primero si existe un vehículo con esa placa
        Vehicle vehicle = vehicleService.findByPlate(loanDTO.getVehicle().getPlate())
                .map(existingVehicle -> {
                    // Si existe, actualizamos sus datos
                    VehicleDTO updateDTO = loanDTO.getVehicle();
                    updateDTO.setVehicleId(existingVehicle.getVehicleId());
                    return vehicleService.updateVehicle(updateDTO);
                })
                .orElseGet(() -> {
                    // Si no existe, creamos uno nuevo
                    return vehicleService.saveVehicle(loanDTO.getVehicle());
                });

        // Crear el préstamo
        Loan loan = new Loan();
        loan.setClient(client);
        loan.setVehicle(vehicle);
        loan.setLoanAmount(loanDTO.getLoanAmount());
        loan.setInterestRate(loanDTO.getInterestRate());
        loan.setNotes(loanDTO.getNotes());
        loan.setLoanStatus(loanDTO.getLoanStatus() != null ?
                loanDTO.getLoanStatus() :
                LoanStatus.ACTIVO);

        return loanRepository.save(loan);
    }

    public List<Loan> getAllLoans() {
        return loanRepository.findAllByOrderByLoanIdDesc().stream()
                .filter(loan -> !Boolean.FALSE.equals(loan.getActive()))
                .toList();
    }

    public Loan getLoanById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));
    }

    @Transactional
    public Loan updateLoan(Long id, LoanDTO loanDTO) {
        // Obtener el préstamo existente
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        // Actualizar datos del préstamo
        if (loanDTO.getLoanAmount() != null) {
            loan.setLoanAmount(loanDTO.getLoanAmount());
        }
        if (loanDTO.getInterestRate() != null) {
            loan.setInterestRate(loanDTO.getInterestRate());
        }
        if (loanDTO.getNotes() != null) {
            loan.setNotes(loanDTO.getNotes());
        }
        if (loanDTO.getLoanStatus() != null) {
            loan.setLoanStatus(loanDTO.getLoanStatus());
        }

        // Si hay datos de vehículo, actualizar el vehículo existente
        if (loanDTO.getVehicle() != null) {
            // Asegurarse de que el ID del vehículo coincida con el vehículo existente
            VehicleDTO vehicleDTO = loanDTO.getVehicle();
            vehicleDTO.setVehicleId(loan.getVehicle().getVehicleId());

            // Actualizar el vehículo existente
            Vehicle updatedVehicle = vehicleService.updateVehicle(vehicleDTO);
            loan.setVehicle(updatedVehicle);
        }

        // Si hay cambio de cliente
        if (loanDTO.getClientId() != null) {
            Client client = clientService.getById(loanDTO.getClientId());
            loan.setClient(client);
        }

        return loanRepository.save(loan);
    }

    public void deleteLoan(Long id) {
        Loan loan = getLoanById(id);
        loan.setActive(false);
        loanRepository.save(loan);
    }

    public List<Loan> searchByTerm(String term) {
        return loanRepository.findByGeneralSearch(term).stream()
                .filter(loan -> !Boolean.FALSE.equals(loan.getActive()))
                .toList();
    }

    public List<Loan> searchByFilters(String term, String documentNumber) {
        if (term != null && !term.isEmpty()) {
            return loanRepository.findByClientFirstNamesContainingIgnoreCaseOrClientLastNamesContainingIgnoreCase(term, term)
                    .stream().filter(loan -> !Boolean.FALSE.equals(loan.getActive())).toList();
        } else if (documentNumber != null && !documentNumber.isEmpty()) {
            return loanRepository.findByClientDocumentNumberContaining(documentNumber).stream()
                    .filter(loan -> !Boolean.FALSE.equals(loan.getActive())).toList();
        }
        return getAllLoans();
    }

    public List<Loan> findOverdueLoans() {
        return loanRepository.findByLoanStatus(LoanStatus.VENCIDO).stream()
                .filter(loan -> !Boolean.FALSE.equals(loan.getActive()))
                .toList();
    }

    public Long getLastLoanId() {
        return loanRepository.findMaxId();
    }

}
