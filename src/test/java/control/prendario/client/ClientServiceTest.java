package control.prendario.client;

import control.prendario.loan.LoanRepository;
import control.prendario.loan.MachineryLoanRepository;
import control.prendario.payment.MachineryPaymentRepository;
import control.prendario.payment.PaymentRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock private ClientRepository clientRepository;
    @Mock private LoanRepository loanRepository;
    @Mock private MachineryLoanRepository machineryLoanRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private MachineryPaymentRepository machineryPaymentRepository;
    @InjectMocks private ClientService service;

    @Test
    void savesActiveClientWithRegistrationDate() {
        Client client = client(1L, "123");
        when(clientRepository.existsByDocumentNumber("123")).thenReturn(false);
        when(clientRepository.save(client)).thenReturn(client);

        assertSame(client, service.saveClient(client));
        assertTrue(client.getActive());
        assertNotNull(client.getCreatedAt());
    }

    @Test
    void doesNotAllowRegisteringDuplicateDocument() {
        when(clientRepository.existsByDocumentNumber("123")).thenReturn(true);
        assertThrows(DuplicateDocumentException.class,
                () -> service.saveClient(client(1L, "123")));
        verify(clientRepository, never()).save(any());
    }

    @Test
    void findsByIdAndRejectsUnknownId() {
        Client client = client(1L, "123");
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        assertSame(client, service.getById(1L));
        assertThrows(ClientService.ClientNotFoundException.class, () -> service.getById(2L));
    }

    @Test
    void doesNotAllowArchivedClientInActiveOperations() {
        Client client = client(1L, "123");
        client.setActive(false);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        assertThrows(ClientService.ClientNotFoundException.class, () -> service.getActiveById(1L));
    }

    @Test
    void updateClientCopiesFieldsAndValidatesDocumentOnlyWhenChanged() {
        Client actual = client(1L, "123");
        Client changes = client(1L, "123");
        changes.setFirstNames("Nuevo nombre");
        changes.setCity("Cali");
        when(clientRepository.findById(1L)).thenReturn(Optional.of(actual));
        when(clientRepository.save(actual)).thenReturn(actual);

        assertSame(actual, service.updateClient(1L, changes));
        assertEquals("Nuevo nombre", actual.getFirstNames());
        assertEquals("Cali", actual.getCity());
        verify(clientRepository, never()).existsByDocumentNumber(any());

        Client withAnotherDocument = client(1L, "456");
        when(clientRepository.existsByDocumentNumber("456")).thenReturn(true);
        assertThrows(DuplicateDocumentException.class,
                () -> service.updateClient(1L, withAnotherDocument));
    }

    @Test
    void historyIncludesRecordsOfBothTypes() {
        Client client = client(1L, "123");
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(loanRepository.findByClientClientIdOrderByLoanDateDesc(1L)).thenReturn(List.of());
        when(machineryLoanRepository.findByClientClientIdOrderByLoanDateDesc(1L)).thenReturn(List.of());
        when(paymentRepository.findByLoanClientClientIdOrderByPaymentDateDesc(1L)).thenReturn(List.of());
        when(machineryPaymentRepository.findByMachineryLoanClientClientIdOrderByPaymentDateDesc(1L)).thenReturn(List.of());

        assertSame(client, service.getHistory(1L).getClient());
        verify(loanRepository).findByClientClientIdOrderByLoanDateDesc(1L);
        verify(machineryLoanRepository).findByClientClientIdOrderByLoanDateDesc(1L);
        verify(paymentRepository).findByLoanClientClientIdOrderByPaymentDateDesc(1L);
        verify(machineryPaymentRepository).findByMachineryLoanClientClientIdOrderByPaymentDateDesc(1L);
    }

    @Test
    void excludesArchivedAndAppliesSearchPriority() {
        Client active = client(1L, "123");
        Client archivedItem = client(2L, "456");
        archivedItem.setActive(false);
        when(clientRepository.findAll()).thenReturn(List.of(active, archivedItem));
        when(clientRepository.findByFirstNamesContainingIgnoreCaseOrLastNamesContainingIgnoreCase("Ana", "Ana"))
                .thenReturn(List.of(active, archivedItem));

        assertEquals(List.of(active), service.getAllClients());
        assertEquals(List.of(active), service.search("Ana", "123"));
        assertEquals(List.of(active), service.search(null, null));
    }

    @Test
    void searchByDocumentAndCityExcludesArchived() {
        Client active = client(1L, "123");
        Client archivedItem = client(2L, "456");
        archivedItem.setActive(false);
        when(clientRepository.findByDocumentNumberContaining("12")).thenReturn(List.of(active, archivedItem));
        when(clientRepository.findByCityIgnoreCase("Bogotá")).thenReturn(List.of(active, archivedItem));
        when(clientRepository.existsByDocumentNumber("123")).thenReturn(true);

        assertEquals(List.of(active), service.searchByDocumentNumber("12"));
        assertEquals(List.of(active), service.searchByCity("Bogotá"));
        assertTrue(service.documentExists("123"));
    }

    private Client client(Long id, String document) {
        Client client = new Client();
        client.setClientId(id);
        client.setDocumentNumber(document);
        client.setFirstNames("Ana");
        client.setLastNames("Pérez");
        client.setCity("Bogotá");
        client.setPhone("3000000000");
        return client;
    }
}
