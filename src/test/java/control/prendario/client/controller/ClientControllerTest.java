package control.prendario.client.controller;

import control.prendario.client.domain.Client;
import control.prendario.client.dto.ClientHistoryDTO;
import control.prendario.client.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientControllerTest {

    private ClientService service;
    private ClientController controller;

    @BeforeEach
    void setUp() {
        service = mock(ClientService.class);
        controller = new ClientController();
        ReflectionTestUtils.setField(controller, "clientService", service);
    }

    @Test
    void delegatesCrudOperationsAndHistory() {
        Client client = new Client();
        List<Client> clients = List.of(client);
        ClientHistoryDTO history = ClientHistoryDTO.builder().client(client).build();
        when(service.saveClient(client)).thenReturn(client);
        when(service.getAllClients()).thenReturn(clients);
        when(service.getById(1L)).thenReturn(client);
        when(service.getHistory(1L)).thenReturn(history);
        when(service.updateClient(1L, client)).thenReturn(client);
        when(service.deleteClient(1L)).thenReturn(true);

        assertSame(client, controller.createClient(client).getBody());
        assertEquals(clients, controller.listClients().getBody());
        assertSame(client, controller.getClient(1L).getBody());
        assertSame(history, controller.getHistory(1L).getBody());
        assertSame(client, controller.updateClient(1L, client).getBody());
        assertEquals(HttpStatus.OK, controller.deleteClient(1L).getStatusCode());
        verify(service).deleteClient(1L);
    }

    @Test
    void searchCombinesResultsWithoutDuplicatesAndSupportsFilters() {
        Client client = new Client();
        Client byDocument = new Client();
        byDocument.setDocumentNumber("123");
        when(service.searchByName("Ana")).thenReturn(List.of(client));
        when(service.searchByDocumentNumber("Ana")).thenReturn(List.of(client, byDocument));
        when(service.search("Ana", "")).thenReturn(List.of(client));

        assertEquals(List.of(client, byDocument), controller.searchClients("Ana", null, null).getBody());
        assertEquals(List.of(client), controller.searchClients(" ", "Ana", null).getBody());
        assertTrue(controller.searchClients(null, null, null).getBody().isEmpty());
        verify(service).search("Ana", "");
    }

    @Test
    void returnsEmptyListWhenSearchFailsAndNotFoundWhenDeleteFails() {
        when(service.searchByName("Ana")).thenThrow(new RuntimeException("error"));
        when(service.deleteClient(2L)).thenReturn(false);

        assertEquals(HttpStatus.BAD_REQUEST, controller.searchClients("Ana", null, null).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.deleteClient(2L).getStatusCode());
    }
}
