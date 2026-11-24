package control.prendario.client.controller;

import control.prendario.client.domain.Client;
import control.prendario.client.dto.ClientHistoryPage;
import control.prendario.client.dto.ClientHistoryQuery;
import control.prendario.shared.query.PageResult;
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
        var clients = new PageResult<>(List.of(client), 0, 20, 1, 1);
        var history = new ClientHistoryPage(client, null, null, null, null);
        var query = new ClientHistoryQuery(null, null, null, null, null, null);
        when(service.saveClient(client)).thenReturn(client);
        when(service.pageClients(any())).thenReturn(clients);
        when(service.getById(1L)).thenReturn(client);
        when(service.getHistory(1L, query)).thenReturn(history);
        when(service.updateClient(1L, client)).thenReturn(client);
        when(service.deleteClient(1L)).thenReturn(true);

        assertSame(client, controller.createClient(client).getBody());
        assertEquals(clients, controller.listClients(null).getBody());
        assertSame(client, controller.getClient(1L).getBody());
        assertSame(history, controller.getHistory(1L, query).getBody());
        assertSame(client, controller.updateClient(1L, client).getBody());
        assertEquals(HttpStatus.OK, controller.deleteClient(1L).getStatusCode());
        verify(service).deleteClient(1L);
    }

    @Test
    void returnsNotFoundWhenDeleteFails() {
        when(service.deleteClient(2L)).thenReturn(false);

        assertEquals(HttpStatus.NOT_FOUND, controller.deleteClient(2L).getStatusCode());
    }
}
