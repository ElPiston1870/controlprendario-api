package control.prendario.loan;

import control.prendario.client.Client;
import control.prendario.client.ClientService;
import control.prendario.vehicle.Vehicle;
import control.prendario.vehicle.VehicleDTO;
import control.prendario.vehicle.VehicleService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock private LoanRepository loanRepository;
    @Mock private ClientService clientService;
    @Mock private VehicleService vehicleService;
    @Mock private MachineryLoanRepository machineRepository;
    @InjectMocks private LoanService service;
    @InjectMocks private MachineryLoanService machineService;

    @Test
    void createsVehicleLoanWithNewVehicleAndDefaultStatus() {
        Client client = new Client();
        client.setClientId(1L);
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleId(4L);
        when(clientService.getActiveById(1L)).thenReturn(client);
        when(vehicleService.findByPlate("ABC123")).thenReturn(Optional.empty());
        when(vehicleService.saveVehicle(any(VehicleDTO.class))).thenReturn(vehicle);
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));
        LoanDTO dto = loanDTO();

        Loan loan = service.saveLoan(dto);

        assertSame(client, loan.getClient());
        assertSame(vehicle, loan.getVehicle());
        assertEquals(LoanStatus.ACTIVO, loan.getLoanStatus());
        verify(vehicleService).saveVehicle(dto.getVehicle());
    }

    @Test
    void createsVehicleLoanUpdatingExistingVehicleAndGivenStatus() {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleId(7L);
        Client client = new Client();
        when(clientService.getActiveById(1L)).thenReturn(client);
        when(vehicleService.findByPlate("ABC123")).thenReturn(Optional.of(vehicle));
        when(vehicleService.updateVehicle(any(VehicleDTO.class))).thenReturn(vehicle);
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));
        LoanDTO dto = loanDTO();
        dto.setLoanStatus(LoanStatus.PENDIENTE);

        Loan loan = service.saveLoan(dto);

        assertEquals(LoanStatus.PENDIENTE, loan.getLoanStatus());
        assertEquals(7L, dto.getVehicle().getVehicleId());
        verify(vehicleService).updateVehicle(dto.getVehicle());
    }

    @Test
    void updatesVehicleLoanAndAllowsChangingClient() {
        Loan existingItem = new Loan();
        existingItem.setLoanId(2L);
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleId(3L);
        existingItem.setVehicle(vehicle);
        when(loanRepository.findById(2L)).thenReturn(Optional.of(existingItem));
        when(clientService.getById(9L)).thenReturn(new Client());
        when(vehicleService.updateVehicle(any(VehicleDTO.class))).thenAnswer(inv -> {
            Vehicle updated = new Vehicle();
            updated.setVehicleId(3L);
            return updated;
        });
        when(loanRepository.save(existingItem)).thenReturn(existingItem);
        LoanDTO dto = loanDTO();
        dto.setLoanStatus(LoanStatus.VENCIDO);
        dto.setClientId(9L);

        assertSame(existingItem, service.updateLoan(2L, dto));
        assertEquals(LoanStatus.VENCIDO, existingItem.getLoanStatus());
        assertEquals(3L, dto.getVehicle().getVehicleId());
        assertNotNull(existingItem.getClient());
    }

    @Test
    void excludesArchivedLoansAndQueriesSearchOverdueAndMaxId() {
        Loan active = new Loan();
        Loan archivedItem = new Loan();
        archivedItem.setActive(false);
        when(loanRepository.findAllByOrderByLoanIdDesc()).thenReturn(List.of(active, archivedItem));
        when(loanRepository.findByLoanStatus(LoanStatus.VENCIDO)).thenReturn(List.of(active, archivedItem));
        when(loanRepository.findByGeneralSearch("Ana")).thenReturn(List.of(active, archivedItem));
        when(loanRepository.findByClientFirstNamesContainingIgnoreCaseOrClientLastNamesContainingIgnoreCase("Ana", "Ana"))
                .thenReturn(List.of(active, archivedItem));
        when(loanRepository.findByClientDocumentNumberContaining("123")).thenReturn(List.of(active, archivedItem));
        when(loanRepository.findMaxId()).thenReturn(10L);

        assertEquals(List.of(active), service.getAllLoans());
        assertEquals(List.of(active), service.findOverdueLoans());
        assertEquals(List.of(active), service.searchByTerm("Ana"));
        assertEquals(List.of(active), service.searchByFilters("Ana", "123"));
        assertEquals(List.of(active), service.searchByFilters(null, "123"));
        assertEquals(List.of(active), service.searchByFilters(null, null));
        assertEquals(10L, service.getLastLoanId());
    }

    @Test
    void createsAndUpdatesMachineryLoan() {
        Client client = new Client();
        when(clientService.getActiveById(1L)).thenReturn(client);
        when(machineRepository.save(any(MachineryLoan.class))).thenAnswer(inv -> inv.getArgument(0));
        MachineryLoanDTO dto = new MachineryLoanDTO();
        dto.setClientId(1L);
        dto.setMachineType("Tractor");
        dto.setMachineBrand("Marca");
        dto.setLoanAmount(new BigDecimal("1000"));

        MachineryLoan machine = machineService.createMachineryLoan(dto);
        assertSame(client, machine.getClient());
        assertEquals(new BigDecimal("5.0"), machine.getInterestRate());
        assertEquals(LoanStatus.ACTIVO, machine.getLoanStatus());

        machine.setMachineryLoanId(3L);
        when(machineRepository.findById(3L)).thenReturn(Optional.of(machine));
        when(clientService.getById(2L)).thenReturn(new Client());
        dto.setClientId(2L);
        dto.setInterestRate(new BigDecimal("8"));
        dto.setLoanStatus(LoanStatus.VENCIDO);
        dto.setNotes("Actualizado");
        when(machineRepository.save(machine)).thenReturn(machine);

        assertSame(machine, machineService.updateMachineryLoan(3L, dto));
        assertEquals(LoanStatus.VENCIDO, machine.getLoanStatus());
        assertEquals(new BigDecimal("8"), machine.getInterestRate());
        assertEquals("Actualizado", machine.getNotes());
        assertNotSame(client, machine.getClient());
    }

    @Test
    void filtersMachineryLoansAndDetectsOverdue() {
        MachineryLoan active = new MachineryLoan();
        active.setLoanStatus(LoanStatus.ACTIVO);
        active.setDueDate(LocalDate.now().minusDays(1));
        MachineryLoan future = new MachineryLoan();
        future.setLoanStatus(LoanStatus.ACTIVO);
        future.setDueDate(LocalDate.now().plusDays(1));
        MachineryLoan archivedItem = new MachineryLoan();
        archivedItem.setActive(false);
        when(machineRepository.findAll()).thenReturn(List.of(active, future, archivedItem));
        when(machineRepository.findByLoanStatus(LoanStatus.VENCIDO)).thenReturn(List.of(active, archivedItem));
        when(machineRepository.findAllByOrderByMachineryLoanIdDesc()).thenReturn(List.of(active, archivedItem));
        when(machineRepository.searchByClient("Ana")).thenReturn(List.of(active, archivedItem));
        when(machineRepository.findByGeneralSearch("Ana")).thenReturn(List.of(active, archivedItem));
        when(machineRepository.findByClientFirstNamesContainingIgnoreCaseOrClientLastNamesContainingIgnoreCase("Ana", "Ana"))
                .thenReturn(List.of(active, archivedItem));
        when(machineRepository.findByClientDocumentNumberContaining("123")).thenReturn(List.of(active, archivedItem));
        when(machineRepository.findMaxId()).thenReturn(11L);

        assertEquals(List.of(active), machineService.getOverdueLoans());
        assertEquals(List.of(active), machineService.findOverdueLoans());
        assertEquals(List.of(active), machineService.getAllMachineryLoans());
        assertEquals(List.of(active), machineService.searchByClient("Ana"));
        assertEquals(List.of(active), machineService.searchByTerm("Ana"));
        assertEquals(List.of(active), machineService.searchByFilters("Ana", "123"));
        assertEquals(List.of(active), machineService.searchByFilters(null, "123"));
        assertEquals(List.of(active), machineService.searchByFilters(null, null));
        assertEquals(11L, machineService.getLastLoanId());
    }

    private LoanDTO loanDTO() {
        LoanDTO dto = new LoanDTO();
        dto.setClientId(1L);
        dto.setLoanAmount(new BigDecimal("1000"));
        dto.setInterestRate(new BigDecimal("5"));
        VehicleDTO vehicle = new VehicleDTO();
        vehicle.setPlate("ABC123");
        vehicle.setVehicleType("AUTOMOVIL");
        dto.setVehicle(vehicle);
        return dto;
    }
}
