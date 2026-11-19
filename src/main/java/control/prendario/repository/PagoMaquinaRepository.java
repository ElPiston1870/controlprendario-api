package control.prendario.repository;

import control.prendario.model.Pago;
import control.prendario.model.PagoMaquina;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagoMaquinaRepository extends JpaRepository<PagoMaquina, Long> {
    List<PagoMaquina> findByPrestamoMaquinaIdPrestamoMaquinaOrderByFechaPagoDesc(Long idPrestamoMaquina);
    List<PagoMaquina> findByPrestamoMaquinaClienteIdClienteOrderByFechaPagoDesc(Long idCliente);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PagoMaquina p WHERE p.idPagoMaquina = :idPago")
    Optional<PagoMaquina> findByIdForUpdate(@Param("idPago") Long idPago);

    @Query("SELECT SUM(p.montoPagado) FROM PagoMaquina p " +
            "WHERE p.prestamoMaquina.idPrestamoMaquina = :idPrestamoMaquina " +
            "AND p.tipoPago = :tipoPago " +
            "AND (p.estadoPago IS NULL OR p.estadoPago <> control.prendario.model.EstadoPago.CORREGIDO)")
    BigDecimal sumMontoPagadoByPrestamoAndTipoPago(Long idPrestamoMaquina, Pago.TipoPago tipoPago);

    @Query("SELECT SUM(p.montoPagado) FROM PagoMaquina p WHERE p.tipoPago = :tipoPago " +
            "AND (p.estadoPago IS NULL OR p.estadoPago <> control.prendario.model.EstadoPago.CORREGIDO)")
    BigDecimal sumMontoPagadoByTipoPago(Pago.TipoPago tipoPago);

    @Query("SELECT DISTINCT p FROM PagoMaquina p " +
            "LEFT JOIN FETCH p.prestamoMaquina pr " +
            "LEFT JOIN FETCH pr.cliente " +
            "ORDER BY p.fechaPago DESC")
    List<PagoMaquina> findAllPagosWithDetails();

    @Query("SELECT COALESCE(SUM(p.montoPagado), 0) FROM PagoMaquina p " +
            "WHERE p.estadoPago IS NULL OR p.estadoPago <> control.prendario.model.EstadoPago.CORREGIDO")
    BigDecimal sumTotalPagos();

}
