package control.prendario.vehicle.repository;

import control.prendario.vehicle.domain.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {
    boolean existsByPlate(String plate);
    Optional<Vehicle> findByPlate(String plate);
}