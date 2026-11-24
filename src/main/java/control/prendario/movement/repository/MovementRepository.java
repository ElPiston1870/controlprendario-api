package control.prendario.movement.repository;

import control.prendario.movement.domain.Movement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MovementRepository extends JpaRepository<Movement, Long>, JpaSpecificationExecutor<Movement> {

    List<Movement> findAllByOrderByMovementDateDesc();

    @Query("SELECT COALESCE(SUM(CASE WHEN m.movementType = 'ENTRADA' THEN m.amount ELSE -m.amount END), 0) " +
            "FROM Movement m")
    BigDecimal calculateBalance();
}
