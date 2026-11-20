package control.prendario.controller;

import control.prendario.DTO.HistorialClienteDTO;
import control.prendario.model.Cliente;
import control.prendario.service.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClienteControllerTest {

    private ClienteService service;
    private ClienteController controller;

    @BeforeEach
    void setUp() {
        service = mock(ClienteService.class);
        controller = new ClienteController();
        ReflectionTestUtils.setField(controller, "clienteService", service);
    }

    @Test
    void delegaOperacionesCrudEHistorial() {
        Cliente cliente = new Cliente();
        List<Cliente> clientes = List.of(cliente);
        HistorialClienteDTO historial = HistorialClienteDTO.builder().cliente(cliente).build();
        when(service.guardarCliente(cliente)).thenReturn(cliente);
        when(service.obtenerTodos()).thenReturn(clientes);
        when(service.obtenerPorId(1L)).thenReturn(cliente);
        when(service.consultarHistorial(1L)).thenReturn(historial);
        when(service.actualizarCliente(1L, cliente)).thenReturn(cliente);
        when(service.eliminarCliente(1L)).thenReturn(true);

        assertSame(cliente, controller.crearCliente(cliente).getBody());
        assertEquals(clientes, controller.listarClientes().getBody());
        assertSame(cliente, controller.obtenerCliente(1L).getBody());
        assertSame(historial, controller.consultarHistorial(1L).getBody());
        assertSame(cliente, controller.actualizarCliente(1L, cliente).getBody());
        assertEquals(HttpStatus.OK, controller.eliminarCliente(1L).getStatusCode());
        verify(service).eliminarCliente(1L);
    }

    @Test
    void buscarCombinaResultadosSinDuplicadosYAdmiteFiltros() {
        Cliente cliente = new Cliente();
        Cliente porDocumento = new Cliente();
        porDocumento.setNumeroDocumento("123");
        when(service.buscarPorNombre("Ana")).thenReturn(List.of(cliente));
        when(service.buscarPorNumeroDocumento("Ana")).thenReturn(List.of(cliente, porDocumento));
        when(service.buscar("Ana", "")).thenReturn(List.of(cliente));

        assertEquals(List.of(cliente, porDocumento), controller.buscarClientes("Ana", null, null).getBody());
        assertEquals(List.of(cliente), controller.buscarClientes(" ", "Ana", null).getBody());
        assertTrue(controller.buscarClientes(null, null, null).getBody().isEmpty());
        verify(service).buscar("Ana", "");
    }

    @Test
    void retornaListaVaciaCuandoLaBusquedaFallaYNotFoundSiEliminacionFalla() {
        when(service.buscarPorNombre("Ana")).thenThrow(new RuntimeException("error"));
        when(service.eliminarCliente(2L)).thenReturn(false);

        assertEquals(HttpStatus.BAD_REQUEST, controller.buscarClientes("Ana", null, null).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.eliminarCliente(2L).getStatusCode());
    }
}
