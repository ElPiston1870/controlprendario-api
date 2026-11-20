package control.prendario;

import control.prendario.client.Client;
import control.prendario.client.ClientRepository;
import control.prendario.client.ClientService;
import control.prendario.loan.Loan;
import control.prendario.loan.LoanRepository;
import control.prendario.loan.LoanService;
import control.prendario.loan.MachineryLoan;
import control.prendario.loan.MachineryLoanRepository;
import control.prendario.loan.MachineryLoanService;
import control.prendario.payment.MachineryPaymentRepository;
import control.prendario.payment.PaymentRepository;
import control.prendario.vehicle.Vehicle;
import control.prendario.vehicle.VehicleRepository;
import control.prendario.vehicle.VehicleService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoricRetentionTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private MachineryLoanRepository machineryLoanRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private MachineryPaymentRepository machineryPaymentRepository;
    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private ClientService clientService;
    @InjectMocks
    private LoanService loanService;
    @InjectMocks
    private MachineryLoanService machineryLoanService;
    @InjectMocks
    private VehicleService vehicleService;

    @Test
    void archivesClientWithoutDeletingRecord() {
        Client client = new Client();
        client.setClientId(1L);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        clientService.deleteClient(1L);

        assertFalse(client.getActive());
        verify(clientRepository).save(client);
        verify(clientRepository, never()).deleteById(1L);
    }

    @Test
    void archivesLoansAndVehiclesWithoutDeletingThem() {
        Loan loan = new Loan();
        loan.setLoanId(2L);
        when(loanRepository.findById(2L)).thenReturn(Optional.of(loan));

        MachineryLoan machinery = new MachineryLoan();
        machinery.setMachineryLoanId(3L);
        when(machineryLoanRepository.findById(3L)).thenReturn(Optional.of(machinery));

        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleId(4L);
        when(vehicleRepository.findById(4L)).thenReturn(Optional.of(vehicle));

        loanService.deleteLoan(2L);
        machineryLoanService.deleteMachineryLoan(3L);
        vehicleService.deleteVehicle(4L);

        assertFalse(loan.getActive());
        assertFalse(machinery.getActive());
        assertFalse(vehicle.getActive());
        verify(loanRepository).save(loan);
        verify(loanRepository, never()).deleteById(2L);
        verify(machineryLoanRepository, never()).deleteById(3L);
        verify(vehicleRepository, never()).deleteById(4L);
    }
}
