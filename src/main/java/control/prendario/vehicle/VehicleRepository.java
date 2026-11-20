package control.prendario.vehicle;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    boolean existsByPlate(String plate);
    Optional<Vehicle> findByPlate(String plate);
}