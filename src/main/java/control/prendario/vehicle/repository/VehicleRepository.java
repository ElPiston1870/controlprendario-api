package control.prendario.vehicle.repository;

import control.prendario.vehicle.domain.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    boolean existsByPlate(String plate);
    Optional<Vehicle> findByPlate(String plate);
}