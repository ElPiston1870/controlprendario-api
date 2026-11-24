package control.prendario.shared.query;

import control.prendario.audit.domain.AuditEvent;
import control.prendario.audit.service.AuditEventService;
import control.prendario.client.domain.Client;
import control.prendario.client.dto.ClientHistoryQuery;
import control.prendario.client.service.ClientService;
import control.prendario.collateral.service.CollateralInventoryService;
import control.prendario.loan.domain.Loan;
import control.prendario.loan.domain.LoanStatus;
import control.prendario.loan.domain.MachineryLoan;
import control.prendario.loan.service.LoanService;
import control.prendario.loan.service.MachineryLoanService;
import control.prendario.movement.domain.Movement;
import control.prendario.movement.service.MovementService;
import control.prendario.payment.domain.MachineryPayment;
import control.prendario.payment.domain.Payment;
import control.prendario.payment.service.InterestCalculationService;
import control.prendario.payment.service.MachineryInterestCalculationService;
import control.prendario.payment.service.MachineryPaymentService;
import control.prendario.payment.service.PaymentService;
import control.prendario.vehicle.domain.Vehicle;
import control.prendario.vehicle.service.VehicleService;
import control.prendario.vehicle.mapper.VehicleMapper;
import org.springframework.boot.test.mock.mockito.MockBean;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(showSql = false, properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Import({ClientService.class, LoanService.class, MachineryLoanService.class, VehicleService.class,
        PaymentService.class, MachineryPaymentService.class, InterestCalculationService.class,
        MachineryInterestCalculationService.class, CollateralInventoryService.class,
        MovementService.class, AuditEventService.class})
class CatalogPaginationTest {
    @MockBean VehicleMapper vehicleMapper;
    @Autowired EntityManager em;
    @Autowired ClientService clients;
    @Autowired LoanService loans;
    @Autowired MachineryLoanService machines;
    @Autowired VehicleService vehicles;
    @Autowired PaymentService payments;
    @Autowired MachineryPaymentService machinePayments;
    @Autowired CollateralInventoryService collateral;
    @Autowired MovementService movements;
    @Autowired AuditEventService events;

    private Client ana;
    private Client other;
    private Loan car;
    private MachineryLoan tractor;
    private Payment carPayment;
    private MachineryPayment tractorPayment;

    @BeforeEach
    void fixtures() {
        ana = client("Ana María", "Pérez", "12345", true);
        other = client("Ana", "Pérez", "99999", true);
        client("Ana", "Pérez", "54321", false);

        Vehicle vehicle = new Vehicle();
        vehicle.setPlate("ABC123");
        vehicle.setBrand("Toyota");
        vehicle.setLine("Hilux");
        vehicle.setActive(true);
        em.persist(vehicle);

        car = new Loan();
        car.setClient(ana);
        car.setVehicle(vehicle);
        car.setLoanAmount(BigDecimal.TEN);
        car.setLoanStatus(LoanStatus.VENCIDO);
        em.persist(car);
        tractor = new MachineryLoan();
        tractor.setClient(ana);
        tractor.setMachineType("Tractor");
        tractor.setMachineBrand("John Deere");
        tractor.setLoanAmount(BigDecimal.TEN);
        tractor.setLoanStatus(LoanStatus.PAGADO);
        em.persist(tractor);

        carPayment = vehiclePayment(car, Payment.PaymentType.CAPITAL);
        vehiclePayment(car, Payment.PaymentType.INTERES);
        tractorPayment = new MachineryPayment();
        tractorPayment.setMachineryLoan(tractor);
        tractorPayment.setAmountPaid(BigDecimal.ONE);
        tractorPayment.setPaymentType(Payment.PaymentType.CAPITAL);
        tractorPayment.setPaymentMethod(Payment.PaymentMethod.EFECTIVO);
        em.persist(tractorPayment);

        Movement movement = new Movement();
        movement.setMovementType(Movement.MovementType.ENTRADA);
        movement.setAmount(BigDecimal.ONE);
        em.persist(movement);
        AuditEvent event = new AuditEvent();
        event.setUsername("encargado");
        event.setAction("ACTUALIZAR");
        event.setResource("CLIENTE");
        event.setResult("EXITO");
        em.persist(event);
        em.flush();
        em.clear();
    }

    @Test
    void filtersClientsBeforeCountingAndPagingAndTreatsWildcardsLiterally() {
        var first = clients.pageClients(query(0, "ana PÉREZ", null, null, null, null, null, null, null, null));
        var second = clients.pageClients(query(1, "ana PÉREZ", null, null, null, null, null, null, null, null));
        assertThat(first.totalElements()).isEqualTo(2);
        assertThat(first.totalPages()).isEqualTo(2);
        assertThat(first.content()).extracting(Client::getClientId).containsExactly(other.getClientId());
        assertThat(second.content()).extracting(Client::getClientId).containsExactly(ana.getClientId());
        assertThat(clients.pageClients(query(0, null, "MARÍA pérez", "123", null, null, null, null, null, null))
                .content()).extracting(Client::getClientId).containsExactly(ana.getClientId());
        assertThat(clients.pageClients(query(0, null, "MARÍA pérez", "999", null, null, null, null, null, null))
                .content()).isEmpty();
        assertThat(clients.pageClients(term("%")).content()).isEmpty();
        assertThat(BrowseQuery.page(-1, 200, Sort.unsorted()).getPageSize()).isEqualTo(100);
        assertThat(BrowseQuery.page(-1, null, Sort.unsorted()).getPageNumber()).isZero();
        assertThatThrownBy(() -> BrowseQuery.page(Integer.MAX_VALUE, 20, Sort.unsorted()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findsLoansByClientCollateralNumberStatusAndDates() {
        var carQuery = query(0, "pÉrez abc123", null, null, car.getLoanId(), "VENCIDO",
                LocalDate.now(), car.getDueDate().toLocalDate(), null, null);
        assertThat(loans.pageLoans(carQuery, true).content()).extracting(Loan::getLoanId).containsExactly(car.getLoanId());
        assertThat(loans.pageLoans(term(car.getLoanId().toString()), false).totalElements()).isEqualTo(1);
        assertThat(loans.pageLoans(term("XYZ000"), true).content()).isEmpty();
        var tractorQuery = query(0, "john deere", null, null, null, "PAGADO",
                LocalDate.now(), tractor.getDueDate(), null, null);
        assertThat(machines.pageLoans(tractorQuery, false).content()).extracting(MachineryLoan::getMachineryLoanId)
                .containsExactly(tractor.getMachineryLoanId());
        assertThat(machines.pageLoans(term("tractor"), true).content()).isEmpty();
        assertThat(vehicles.pageVehicles(term("abc123 toyota")).totalElements()).isEqualTo(1);
    }

    @Test
    void filtersPaymentsAndPagesEachSectionOfClientHistoryIndependently() {
        var filter = query(0, "ana 12345", "pérez", "123", null, null,
                LocalDate.now(), null, "capital", "efectivo");
        assertThat(payments.pagePayments(filter).content()).extracting(Payment::getPaymentId)
                .containsExactly(carPayment.getPaymentId());
        assertThat(machinePayments.pagePayments(filter).content()).extracting(MachineryPayment::getMachineryPaymentId)
                .containsExactly(tractorPayment.getMachineryPaymentId());
        var history = clients.getHistory(ana.getClientId(), new ClientHistoryQuery(null, 1, 0, 0, 1, 0));
        assertThat(history.pagosVehiculares().totalPages()).isEqualTo(2);
        assertThat(history.pagosVehiculares().number()).isEqualTo(1);
        assertThat(history.pagosVehiculares().content()).extracting(Payment::getPaymentId).containsExactly(carPayment.getPaymentId());
        assertThat(history.pagosMaquinaria().number()).isZero();
        assertThat(history.pagosMaquinaria().totalElements()).isEqualTo(1);
        assertThat(history.prestamosVehiculares().totalElements()).isEqualTo(1);
        assertThat(history.prestamosMaquinaria().totalElements()).isEqualTo(1);
        assertThat(payments.pagePayments(term("pérez")).totalPages()).isEqualTo(2);
    }

    @Test
    void pagesInventoryAcrossBothCollateralTypesAndPaginatesOtherLists() {
        var first = collateral.getInventory(term("pÉrez 12345"));
        var second = collateral.getInventory(query(1, "pÉrez 12345", null, null, null, null, null, null, null, null));
        assertThat(first.totalElements()).isEqualTo(2);
        assertThat(first.content()).hasSize(1);
        assertThat(second.content()).hasSize(1);
        assertThat(List.of(first.content().get(0).getCollateralType(), second.content().get(0).getCollateralType()))
                .containsExactlyInAnyOrder("VEHICULO", "MAQUINARIA");
        assertThat(collateral.getInventory(term("hilux ABC123")).content().get(0).isInCollateral()).isTrue();
        assertThat(collateral.getInventory(term("john deere")).content().get(0).isInCollateral()).isFalse();
        assertThat(collateral.getInventory(term("ninguna")).content()).isEmpty();
        assertThat(movements.pageMovements(query(0, "entrada", null, null, null, null, LocalDate.now(), null, null, null))
                .totalElements()).isEqualTo(1);
        assertThat(events.pageEvents(term("encargado cliente")).totalElements()).isEqualTo(1);
    }

    private Client client(String name, String lastName, String document, boolean active) {
        Client client = new Client();
        client.setFirstNames(name);
        client.setLastNames(lastName);
        client.setDocumentNumber(document);
        client.setActive(active);
        em.persist(client);
        return client;
    }

    private Payment vehiclePayment(Loan loan, Payment.PaymentType type) {
        Payment payment = new Payment();
        payment.setLoan(loan);
        payment.setAmountPaid(BigDecimal.ONE);
        payment.setPaymentType(type);
        payment.setPaymentMethod(Payment.PaymentMethod.EFECTIVO);
        em.persist(payment);
        return payment;
    }

    private BrowseQuery term(String term) {
        return query(0, term, null, null, null, null, null, null, null, null);
    }

    private BrowseQuery query(int page, String term, String name, String document, Long id, String status,
                              LocalDate date, LocalDate dueDate, String paymentType, String paymentMethod) {
        return new BrowseQuery(page, 1, term, name, null, document, id, status, date, dueDate,
                paymentType, paymentMethod, null);
    }
}
