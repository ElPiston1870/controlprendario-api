package control.prendario.repository;

import control.prendario.model.EstadoPrestamo;
import control.prendario.model.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    @Query("SELECT p FROM Prestamo p JOIN p.cliente c " +
            "WHERE LOWER(c.nombres) LIKE LOWER(CONCAT('%', :termino, '%')) " +
            "OR LOWER(c.apellidos) LIKE LOWER(CONCAT('%', :termino, '%')) " +
            "OR LOWER(c.numeroDocumento) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<Prestamo> findByBusquedaGeneral(String termino);

    @Query("SELECT COALESCE(SUM(p.montoPrestamo), 0) FROM Prestamo p")
    BigDecimal sumTotalPrestamos();

    @Query("SELECT COALESCE(SUM(p.montoPrestamo), 0) FROM Prestamo p WHERE p.estadoPrestamo IN (:estados)")
    BigDecimal sumarMontosPorEstado(@Param("estados") EstadoPrestamo... estados);

    // Consulta personalizada para obtener el ID más alto
    @Query("SELECT MAX(p.idPrestamo) FROM Prestamo p")
    Long findMaxId();

    List<Prestamo> findByEstadoPrestamo(EstadoPrestamo estadoPrestamo);
    List<Prestamo> findAllByOrderByIdPrestamoDesc();
    List<Prestamo> findByClienteNombresContainingIgnoreCaseOrClienteApellidosContainingIgnoreCase(String nombres, String apellidos);
    List<Prestamo> findByClienteNumeroDocumentoContaining(String numeroDocumento);
}
