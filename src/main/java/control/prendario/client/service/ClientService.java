package control.prendario.client.service;

import control.prendario.client.domain.Client;
import control.prendario.client.domain.DuplicateDocumentException;
import control.prendario.client.dto.ClientHistoryDTO;
import control.prendario.client.repository.ClientRepository;
import control.prendario.loan.repository.LoanRepository;
import control.prendario.loan.repository.MachineryLoanRepository;
import control.prendario.payment.repository.MachineryPaymentRepository;
import control.prendario.payment.repository.PaymentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private MachineryLoanRepository machineryLoanRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private MachineryPaymentRepository machineryPaymentRepository;

    public Client saveClient(Client client) {
        if (documentExists(client.getDocumentNumber())) {
            throw new DuplicateDocumentException("Ya existe un cliente con ese número de documento");
        }
        client.setCreatedAt(LocalDateTime.now());
        client.setActive(true);
        return clientRepository.save(client);
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll().stream()
                .filter(client -> !Boolean.FALSE.equals(client.getActive()))
                .toList();
    }

    public Client getById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("Cliente con ID " + id + " no encontrado"));
    }

    public Client getActiveById(Long id) {
        Client client = getById(id);
        if (Boolean.FALSE.equals(client.getActive())) {
            throw new ClientNotFoundException("El cliente está archivado");
        }
        return client;
    }

    public ClientHistoryDTO getHistory(Long id) {
        Client client = getById(id);

        return ClientHistoryDTO.builder()
                .client(client)
                .vehicleLoans(loanRepository
                        .findByClientClientIdOrderByLoanDateDesc(id))
                .machineryLoans(machineryLoanRepository
                        .findByClientClientIdOrderByLoanDateDesc(id))
                .vehiclePayments(paymentRepository
                        .findByLoanClientClientIdOrderByPaymentDateDesc(id))
                .machineryPayments(machineryPaymentRepository
                        .findByMachineryLoanClientClientIdOrderByPaymentDateDesc(id))
                .build();
    }

    public class ClientNotFoundException extends RuntimeException {
        public ClientNotFoundException(String message) {
            super(message);
        }
    }
    public boolean documentExists(String documentNumber) {
        return clientRepository.existsByDocumentNumber(documentNumber);
    }

    public Client updateClient(Long id, Client updatedClient) {
        Client existingClient = getById(id);

        // Validar que si cambia el número de documento, no exista ya
        if (!existingClient.getDocumentNumber().equals(updatedClient.getDocumentNumber())
                && documentExists(updatedClient.getDocumentNumber())) {
            throw new DuplicateDocumentException("Ya existe un cliente con ese número de documento");
        }

        // Actualizar los campos
        existingClient.setDocumentType(updatedClient.getDocumentType());
        existingClient.setDocumentNumber(updatedClient.getDocumentNumber());
        existingClient.setFirstNames(updatedClient.getFirstNames());
        existingClient.setLastNames(updatedClient.getLastNames());
        existingClient.setBirthDate(updatedClient.getBirthDate());
        existingClient.setAddress(updatedClient.getAddress());
        existingClient.setCity(updatedClient.getCity());
        existingClient.setPhone(updatedClient.getPhone());
        existingClient.setEmail(updatedClient.getEmail());

        return clientRepository.save(existingClient);
    }
    public boolean deleteClient(Long id) {
        Client client = getById(id);
        client.setActive(false);
        clientRepository.save(client);
        return true;
    }

    public List<Client> searchByName(String term) {
        return clientRepository.findByFirstNamesContainingIgnoreCaseOrLastNamesContainingIgnoreCase(term, term)
                .stream().filter(client -> !Boolean.FALSE.equals(client.getActive())).toList();
    }

    public List<Client> searchByDocumentNumber(String documentNumber) {
        return clientRepository.findByDocumentNumberContaining(documentNumber)
                .stream().filter(client -> !Boolean.FALSE.equals(client.getActive())).toList();
    }

    public List<Client> searchByCity(String city) {
        return clientRepository.findByCityIgnoreCase(city).stream()
                .filter(client -> !Boolean.FALSE.equals(client.getActive())).toList();

    }
    public List<Client> search(String nameFilter, String documentNumber) {
        if (nameFilter != null && !nameFilter.isEmpty()) {
            return searchByName(nameFilter);
        } else if (documentNumber != null && !documentNumber.isEmpty()) {
            return searchByDocumentNumber(documentNumber);
        }
        return getAllClients();
    }
}