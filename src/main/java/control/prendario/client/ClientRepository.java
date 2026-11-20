package control.prendario.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    boolean existsByDocumentNumber(String documentNumber);

    Optional<Client> findByDocumentTypeAndDocumentNumber(String documentType, String documentNumber);

    List<Client> findByFirstNamesContainingIgnoreCaseOrLastNamesContainingIgnoreCase(String firstNames, String lastNames);

    List<Client> findByCityIgnoreCase(String city);

    List<Client> findByDocumentNumberContaining(String documentNumber);

}
