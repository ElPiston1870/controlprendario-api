package control.prendario.service;

import control.prendario.model.Cliente;
import control.prendario.repository.ClienteRepository;
import control.prendario.repository.PagoMaquinaRepository;
import control.prendario.repository.PagoRepository;
import control.prendario.repository.PrestamoMaquinaRepository;
import control.prendario.repository.PrestamoRepository;
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
class ClienteServiceTest {

    @Mock private ClienteRepository clienteRepository;
    @Mock private PrestamoRepository prestamoRepository;
    @Mock private PrestamoMaquinaRepository prestamoMaquinaRepository;
    @Mock private PagoRepository pagoRepository;
    @Mock private PagoMaquinaRepository pagoMaquinaRepository;
    @InjectMocks private ClienteService service;

    @Test
    void guardaClienteActivoConFechaDeRegistro() {
        Cliente cliente = cliente(1L, "123");
        when(clienteRepository.existsByNumeroDocumento("123")).thenReturn(false);
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        assertSame(cliente, service.guardarCliente(cliente));
        assertTrue(cliente.getActivo());
        assertNotNull(cliente.getFechaRegistro());
    }

    @Test
    void noPermiteRegistrarUnDocumentoDuplicado() {
        when(clienteRepository.existsByNumeroDocumento("123")).thenReturn(true);
        assertThrows(DocumentoDuplicadoException.class,
                () -> service.guardarCliente(cliente(1L, "123")));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void buscaPorIdYRechazaIdInexistente() {
        Cliente cliente = cliente(1L, "123");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        assertSame(cliente, service.obtenerPorId(1L));
        assertThrows(ClienteService.ClienteNotFoundException.class, () -> service.obtenerPorId(2L));
    }

    @Test
    void noPermiteUsarClienteArchivadoEnOperacionesActivas() {
        Cliente cliente = cliente(1L, "123");
        cliente.setActivo(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        assertThrows(ClienteService.ClienteNotFoundException.class, () -> service.obtenerActivoPorId(1L));
    }

    @Test
    void actualizarClienteCopiaCamposYValidaDocumentoSoloSiCambia() {
        Cliente actual = cliente(1L, "123");
        Cliente cambios = cliente(1L, "123");
        cambios.setNombres("Nuevo nombre");
        cambios.setCiudad("Cali");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(actual));
        when(clienteRepository.save(actual)).thenReturn(actual);

        assertSame(actual, service.actualizarCliente(1L, cambios));
        assertEquals("Nuevo nombre", actual.getNombres());
        assertEquals("Cali", actual.getCiudad());
        verify(clienteRepository, never()).existsByNumeroDocumento(any());

        Cliente conOtroDocumento = cliente(1L, "456");
        when(clienteRepository.existsByNumeroDocumento("456")).thenReturn(true);
        assertThrows(DocumentoDuplicadoException.class,
                () -> service.actualizarCliente(1L, conOtroDocumento));
    }

    @Test
    void historialIncluyeRegistrosDeAmbosTipos() {
        Cliente cliente = cliente(1L, "123");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(prestamoRepository.findByClienteIdClienteOrderByFechaPrestamoDesc(1L)).thenReturn(List.of());
        when(prestamoMaquinaRepository.findByClienteIdClienteOrderByFechaPrestamoDesc(1L)).thenReturn(List.of());
        when(pagoRepository.findByPrestamoClienteIdClienteOrderByFechaPagoDesc(1L)).thenReturn(List.of());
        when(pagoMaquinaRepository.findByPrestamoMaquinaClienteIdClienteOrderByFechaPagoDesc(1L)).thenReturn(List.of());

        assertSame(cliente, service.consultarHistorial(1L).getCliente());
        verify(prestamoRepository).findByClienteIdClienteOrderByFechaPrestamoDesc(1L);
        verify(prestamoMaquinaRepository).findByClienteIdClienteOrderByFechaPrestamoDesc(1L);
        verify(pagoRepository).findByPrestamoClienteIdClienteOrderByFechaPagoDesc(1L);
        verify(pagoMaquinaRepository).findByPrestamoMaquinaClienteIdClienteOrderByFechaPagoDesc(1L);
    }

    @Test
    void filtraArchivadosYAplicaPrioridadDeBusqueda() {
        Cliente activo = cliente(1L, "123");
        Cliente archivado = cliente(2L, "456");
        archivado.setActivo(false);
        when(clienteRepository.findAll()).thenReturn(List.of(activo, archivado));
        when(clienteRepository.findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase("Ana", "Ana"))
                .thenReturn(List.of(activo, archivado));

        assertEquals(List.of(activo), service.obtenerTodos());
        assertEquals(List.of(activo), service.buscar("Ana", "123"));
        assertEquals(List.of(activo), service.buscar(null, null));
    }

    @Test
    void buscarPorDocumentoYCiudadFiltraArchivados() {
        Cliente activo = cliente(1L, "123");
        Cliente archivado = cliente(2L, "456");
        archivado.setActivo(false);
        when(clienteRepository.findByNumeroDocumentoContaining("12")).thenReturn(List.of(activo, archivado));
        when(clienteRepository.findByCiudadIgnoreCase("Bogotá")).thenReturn(List.of(activo, archivado));
        when(clienteRepository.existsByNumeroDocumento("123")).thenReturn(true);

        assertEquals(List.of(activo), service.buscarPorNumeroDocumento("12"));
        assertEquals(List.of(activo), service.buscarPorCiudad("Bogotá"));
        assertTrue(service.existeDocumento("123"));
    }

    private Cliente cliente(Long id, String documento) {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(id);
        cliente.setNumeroDocumento(documento);
        cliente.setNombres("Ana");
        cliente.setApellidos("Pérez");
        cliente.setCiudad("Bogotá");
        cliente.setTelefono("3000000000");
        return cliente;
    }
}
