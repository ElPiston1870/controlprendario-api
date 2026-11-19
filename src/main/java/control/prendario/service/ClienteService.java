package control.prendario.service;


import control.prendario.DTO.HistorialClienteDTO;
import control.prendario.model.Cliente;
import control.prendario.repository.ClienteRepository;
import control.prendario.repository.PagoMaquinaRepository;
import control.prendario.repository.PagoRepository;
import control.prendario.repository.PrestamoMaquinaRepository;
import control.prendario.repository.PrestamoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private PrestamoMaquinaRepository prestamoMaquinaRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private PagoMaquinaRepository pagoMaquinaRepository;

    public Cliente guardarCliente(Cliente cliente) {
        if (existeDocumento(cliente.getNumeroDocumento())) {
            throw new DocumentoDuplicadoException("Ya existe un cliente con ese número de documento");
        }
        cliente.setFechaRegistro(LocalDateTime.now());
        cliente.setActivo(true);
        return clienteRepository.save(cliente);
    }

    public List<Cliente> obtenerTodos() {
        return clienteRepository.findAll().stream()
                .filter(cliente -> !Boolean.FALSE.equals(cliente.getActivo()))
                .toList();
    }


    public Cliente obtenerPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente con ID " + id + " no encontrado"));
    }

    public Cliente obtenerActivoPorId(Long id) {
        Cliente cliente = obtenerPorId(id);
        if (Boolean.FALSE.equals(cliente.getActivo())) {
            throw new ClienteNotFoundException("El cliente está archivado");
        }
        return cliente;
    }

    public HistorialClienteDTO consultarHistorial(Long id) {
        Cliente cliente = obtenerPorId(id);

        return HistorialClienteDTO.builder()
                .cliente(cliente)
                .prestamosVehiculares(prestamoRepository
                        .findByClienteIdClienteOrderByFechaPrestamoDesc(id))
                .prestamosMaquinaria(prestamoMaquinaRepository
                        .findByClienteIdClienteOrderByFechaPrestamoDesc(id))
                .pagosVehiculares(pagoRepository
                        .findByPrestamoClienteIdClienteOrderByFechaPagoDesc(id))
                .pagosMaquinaria(pagoMaquinaRepository
                        .findByPrestamoMaquinaClienteIdClienteOrderByFechaPagoDesc(id))
                .build();
    }

    public class ClienteNotFoundException extends RuntimeException {
        public ClienteNotFoundException(String message) {
            super(message);
        }
    }
    public boolean existeDocumento(String numeroDocumento) {
        return clienteRepository.existsByNumeroDocumento(numeroDocumento);
    }

    public Cliente actualizarCliente(Long id, Cliente clienteActualizado) {
        Cliente clienteExistente = obtenerPorId(id);

        // Validar que si cambia el número de documento, no exista ya
        if (!clienteExistente.getNumeroDocumento().equals(clienteActualizado.getNumeroDocumento())
                && existeDocumento(clienteActualizado.getNumeroDocumento())) {
            throw new DocumentoDuplicadoException("Ya existe un cliente con ese número de documento");
        }

        // Actualizar los campos
        clienteExistente.setTipoDocumento(clienteActualizado.getTipoDocumento());
        clienteExistente.setNumeroDocumento(clienteActualizado.getNumeroDocumento());
        clienteExistente.setNombres(clienteActualizado.getNombres());
        clienteExistente.setApellidos(clienteActualizado.getApellidos());
        clienteExistente.setFechaNacimiento(clienteActualizado.getFechaNacimiento());
        clienteExistente.setDireccion(clienteActualizado.getDireccion());
        clienteExistente.setCiudad(clienteActualizado.getCiudad());
        clienteExistente.setTelefono(clienteActualizado.getTelefono());
        clienteExistente.setEmail(clienteActualizado.getEmail());

        return clienteRepository.save(clienteExistente);
    }
    public boolean eliminarCliente(Long id) {
        Cliente cliente = obtenerPorId(id);
        cliente.setActivo(false);
        clienteRepository.save(cliente);
        return true;
    }

    public List<Cliente> buscarPorNombre(String termino) {
        return clienteRepository.findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(termino, termino)
                .stream().filter(cliente -> !Boolean.FALSE.equals(cliente.getActivo())).toList();
    }

    public List<Cliente> buscarPorNumeroDocumento(String numeroDocumento) {
        return clienteRepository.findByNumeroDocumentoContaining(numeroDocumento)
                .stream().filter(cliente -> !Boolean.FALSE.equals(cliente.getActivo())).toList();
    }

    public List<Cliente> buscarPorCiudad(String ciudad) {
        return clienteRepository.findByCiudadIgnoreCase(ciudad).stream()
                .filter(cliente -> !Boolean.FALSE.equals(cliente.getActivo())).toList();

    }
    public List<Cliente> buscar(String nombre, String numeroDocumento) {
        if (nombre != null && !nombre.isEmpty()) {
            return buscarPorNombre(nombre);
        } else if (numeroDocumento != null && !numeroDocumento.isEmpty()) {
            return buscarPorNumeroDocumento(numeroDocumento);
        }
        return obtenerTodos();
    }
}