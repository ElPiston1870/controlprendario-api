package control.prendario.client.repository;

import control.prendario.client.domain.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {
    boolean existsByDocumentNumber(String documentNumber);

    Optional<Client> findByDocumentTypeAndDocumentNumber(String documentType, String documentNumber);

    List<Client> findByFirstNamesContainingIgnoreCaseOrLastNamesContainingIgnoreCase(String firstNames, String lastNames);

    List<Client> findByCityIgnoreCase(String city);

    List<Client> findByDocumentNumberContaining(String documentNumber);

}
