package control.prendario.collateral.service;

import control.prendario.client.domain.Client;
import control.prendario.loan.domain.Loan;
import control.prendario.loan.repository.LoanRepository;
import control.prendario.loan.domain.LoanStatus;
import control.prendario.loan.domain.MachineryLoan;
import control.prendario.loan.repository.MachineryLoanRepository;
import control.prendario.vehicle.domain.Vehicle;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CollateralInventoryServiceTest {

    @Test
    void listsVehicleAndMachineryCollateralWithOccupancyStatus() {
        LoanRepository loanRepository = mock(LoanRepository.class);
        MachineryLoanRepository machineryLoanRepository = mock(MachineryLoanRepository.class);
        CollateralInventoryService service =
                new CollateralInventoryService(loanRepository, machineryLoanRepository);

        Client client = new Client();
        client.setClientId(4L);
        client.setFirstNames("Ana");
        client.setLastNames("Pérez");

        Vehicle vehicle = new Vehicle();
        vehicle.setBrand("Toyota");
        vehicle.setLine("Corolla");
        vehicle.setPlate("ABC123");

        Loan vehicleLoan = new Loan();
        vehicleLoan.setLoanId(2L);
        vehicleLoan.setClient(client);
        vehicleLoan.setVehicle(vehicle);
        vehicleLoan.setLoanAmount(new BigDecimal("1000"));
        vehicleLoan.setLoanStatus(LoanStatus.ACTIVO);

        MachineryLoan machinery = new MachineryLoan();
        machinery.setMachineryLoanId(3L);
        machinery.setClient(client);
        machinery.setMachineType("Tractor");
        machinery.setMachineBrand("John Deere");
        machinery.setLoanStatus(LoanStatus.PAGADO);

        when(loanRepository.findAll()).thenReturn(List.of(vehicleLoan));
        when(machineryLoanRepository.findAll()).thenReturn(List.of(machinery));

        var inventory = service.getInventory();

        assertEquals(2, inventory.size());
        assertEquals("VEHICULO", inventory.get(0).getCollateralType());
        assertTrue(inventory.get(0).isInCollateral());
        assertEquals("MAQUINARIA", inventory.get(1).getCollateralType());
        assertTrue(!inventory.get(1).isInCollateral());
    }
}
