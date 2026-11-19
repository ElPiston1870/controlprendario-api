package control.prendario.repository;

import control.prendario.model.Pago;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByPrestamoIdPrestamoOrderByFechaPagoDesc(Long idPrestamo);
    List<Pago> findByPrestamoClienteIdClienteOrderByFechaPagoDesc(Long idCliente);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pago p WHERE p.idPago = :idPago")
    Optional<Pago> findByIdForUpdate(@Param("idPago") Long idPago);

    @Query("SELECT p FROM Pago p WHERE p.prestamo.cliente.nombres LIKE %:termino% " +
            "OR p.prestamo.cliente.apellidos LIKE %:termino% " +
            "OR p.prestamo.cliente.numeroDocumento LIKE %:termino%")
    List<Pago> buscarPorCliente(String termino);

    @Query("SELECT SUM(p.montoPagado) FROM Pago p WHERE p.prestamo.idPrestamo = :idPrestamo " +
            "AND p.tipoPago = :tipoPago AND (p.estadoPago IS NULL OR p.estadoPago <> control.prendario.model.EstadoPago.CORREGIDO)")
    BigDecimal sumMontoPagadoByPrestamoAndTipoPago(Long idPrestamo, Pago.TipoPago tipoPago);

    @Query("SELECT COALESCE(SUM(p.montoPagado), 0) FROM Pago p WHERE p.tipoPago = :tipoPago " +
            "AND (p.estadoPago IS NULL OR p.estadoPago <> control.prendario.model.EstadoPago.CORREGIDO) " +
            "AND (p.esDescuento IS NULL OR p.esDescuento = false)")
    BigDecimal sumMontoPagadoByTipoPago(Pago.TipoPago tipoPago);

    @Query("SELECT DISTINCT p FROM Pago p " +
            "LEFT JOIN FETCH p.prestamo pr " +
            "LEFT JOIN FETCH pr.cliente " +
            "ORDER BY p.fechaPago DESC")
    List<Pago> findAllPagosWithDetails();

    @Query("SELECT COALESCE(SUM(p.montoPagado), 0) FROM Pago p " +
            "WHERE (p.estadoPago IS NULL OR p.estadoPago <> control.prendario.model.EstadoPago.CORREGIDO) " +
            "AND (p.esDescuento IS NULL OR p.esDescuento = false)")
    BigDecimal sumTotalPagos();

    @Query("SELECT p FROM Pago p " +
            "WHERE p.prestamo.idPrestamo = :idPrestamo " +
            "AND p.tipoPago = :tipoPago")
    List<Pago> findByPrestamoAndTipoPago(
            @Param("idPrestamo") Long idPrestamo,
            @Param("tipoPago") Pago.TipoPago tipoPago
    );
}
