package control.prendario.report.service;

import control.prendario.client.domain.Client;
import control.prendario.loan.domain.Loan;
import control.prendario.loan.repository.LoanRepository;
import control.prendario.loan.domain.LoanStatus;
import control.prendario.loan.domain.MachineryLoan;
import control.prendario.loan.repository.MachineryLoanRepository;
import control.prendario.payment.domain.MachineryPayment;
import control.prendario.payment.repository.MachineryPaymentRepository;
import control.prendario.payment.domain.Payment;
import control.prendario.payment.repository.PaymentRepository;
import control.prendario.vehicle.domain.Vehicle;
import control.prendario.vehicle.domain.VehicleType;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportSummaryTest {

    @Test
    void generatesSummaryPdfEvenWithoutLoans() throws Exception {
        ReportService reportService = new ReportService(
                mock(LoanRepository.class),
                mock(MachineryLoanRepository.class),
                mock(PaymentRepository.class),
                mock(MachineryPaymentRepository.class)
        );

        byte[] pdf = reportService.generateLoanSummaryPdf();

        assertTrue(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII).startsWith("%PDF-"));
    }

    @Test
    void generatesSummaryWithVehicleAndMachineryLoans() throws Exception {
        LoanRepository loanRepository = mock(LoanRepository.class);
        MachineryLoanRepository machineRepository = mock(MachineryLoanRepository.class);
        ReportService reportService = new ReportService(loanRepository, machineRepository,
                mock(PaymentRepository.class), mock(MachineryPaymentRepository.class));
        Client client = new Client();
        client.setFirstNames("Ana");
        client.setLastNames("Pérez");
        Vehicle vehicle = new Vehicle();
        vehicle.setBrand("Toyota");
        vehicle.setLine("Corolla");
        vehicle.setPlate("ABC123");
        Loan vehicleLoan = new Loan();
        vehicleLoan.setLoanId(1L);
        vehicleLoan.setClient(client);
        vehicleLoan.setVehicle(vehicle);
        vehicleLoan.setLoanAmount(new BigDecimal("1000"));
        vehicleLoan.setPaidPrincipal(new BigDecimal("200"));
        vehicleLoan.setPendingInterest(new BigDecimal("50"));
        vehicleLoan.setLoanStatus(LoanStatus.ACTIVO);
        Loan archivedItem = new Loan();
        archivedItem.setActive(false);
        MachineryLoan machinery = new MachineryLoan();
        machinery.setMachineryLoanId(2L);
        machinery.setMachineType("Tractor");
        machinery.setMachineBrand("Marca");
        machinery.setLoanAmount(new BigDecimal("2000"));
        machinery.setPendingPrincipal(new BigDecimal("1500"));
        machinery.setLoanStatus(LoanStatus.PAGADO);
        when(loanRepository.findAll()).thenReturn(List.of(vehicleLoan, archivedItem));
        when(machineRepository.findAll()).thenReturn(List.of(machinery));

        byte[] pdf = reportService.generateLoanSummaryPdf();

        assertTrue(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII).startsWith("%PDF-"));
    }

    @Test
    void generatesReceiptsForVehicleAndMachineryPayments() throws Exception {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        MachineryPaymentRepository machinePaymentRepository = mock(MachineryPaymentRepository.class);
        ReportService reportService = new ReportService(mock(LoanRepository.class),
                mock(MachineryLoanRepository.class), paymentRepository, machinePaymentRepository);
        Client client = new Client();
        client.setFirstNames("Ana");
        client.setLastNames("Pérez");
        client.setDocumentNumber("123");
        Vehicle vehicle = new Vehicle();
        vehicle.setBrand("Toyota");
        vehicle.setLine("Corolla");
        vehicle.setModel(2022);
        vehicle.setPlate("ABC123");
        Loan loan = new Loan();
        loan.setLoanId(1L);
        loan.setClient(client);
        loan.setVehicle(vehicle);
        loan.setLoanStatus(LoanStatus.ACTIVO);
        Payment payment = new Payment();
        payment.setPaymentId(5L);
        payment.setLoan(loan);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setAmountPaid(new BigDecimal("100"));
        payment.setPaymentType(Payment.PaymentType.CAPITAL);
        payment.setPaymentMethod(Payment.PaymentMethod.EFECTIVO);
        when(paymentRepository.findById(5L)).thenReturn(Optional.of(payment));

        MachineryLoan machineryLoan = new MachineryLoan();
        machineryLoan.setMachineryLoanId(2L);
        machineryLoan.setClient(client);
        machineryLoan.setMachineType("Tractor");
        machineryLoan.setMachineBrand("Marca");
        MachineryPayment machineryPayment = new MachineryPayment();
        machineryPayment.setMachineryPaymentId(6L);
        machineryPayment.setMachineryLoan(machineryLoan);
        machineryPayment.setPaymentDate(LocalDateTime.now());
        machineryPayment.setAmountPaid(new BigDecimal("200"));
        machineryPayment.setPaymentType(Payment.PaymentType.INTERES);
        machineryPayment.setPaymentMethod(Payment.PaymentMethod.TRANSFERENCIA);
        when(machinePaymentRepository.findById(6L)).thenReturn(Optional.of(machineryPayment));

        assertTrue(isPdf(reportService.generatePaymentReceipt(5L)));
        assertTrue(isPdf(reportService.generateMachineryPaymentReceipt(6L)));
    }

    @Test
    void generatesVehicleAndMachineryContracts() throws Exception {
        LoanRepository loanRepository = mock(LoanRepository.class);
        MachineryLoanRepository machineRepository = mock(MachineryLoanRepository.class);
        ReportService reportService = new ReportService(loanRepository, machineRepository,
                mock(PaymentRepository.class), mock(MachineryPaymentRepository.class));
        Client client = new Client();
        client.setFirstNames("Ana");
        client.setLastNames("Pérez");
        client.setCity("Bogotá");
        client.setDocumentNumber("123");
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleType(VehicleType.AUTOMOVIL);
        vehicle.setBrand("Toyota");
        vehicle.setLine("Corolla");
        vehicle.setModel(2022);
        vehicle.setPlate("ABC123");
        vehicle.setEngineDisplacement(1600);
        vehicle.setColor("Rojo");
        vehicle.setEngineNumber("MOTOR");
        vehicle.setChassisNumber("CHASIS");
        vehicle.setRegistrationPlace("Bogotá");
        vehicle.setOwner("Ana Pérez");
        Loan loan = new Loan();
        loan.setClient(client);
        loan.setVehicle(vehicle);
        loan.setLoanAmount(new BigDecimal("1000"));
        loan.setInterestRate(new BigDecimal("5"));
        loan.setLoanDate(LocalDateTime.now());
        loan.setDueDate(LocalDateTime.now().plusMonths(3));
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        MachineryLoan machine = new MachineryLoan();
        machine.setClient(client);
        machine.setMachineType("Tractor");
        machine.setMachineBrand("Marca");
        machine.setLoanAmount(new BigDecimal("2000"));
        machine.setInterestRate(new BigDecimal("5"));
        machine.setLoanDate(LocalDateTime.now());
        machine.setDueDate(java.time.LocalDate.now().plusMonths(3));
        when(machineRepository.findById(2L)).thenReturn(Optional.of(machine));

        assertTrue(isPdf(reportService.generatePledgeContract(1L)));
        assertTrue(isPdf(reportService.generateMachineryPledgeContract(2L)));
    }

    private boolean isPdf(byte[] bytes) {
        return new String(bytes, 0, 5, java.nio.charset.StandardCharsets.US_ASCII).startsWith("%PDF-");
    }
}
