package control.prendario.vehicle.service;

import control.prendario.vehicle.domain.Vehicle;
import control.prendario.vehicle.domain.VehicleType;
import control.prendario.vehicle.dto.VehicleDTO;
import control.prendario.vehicle.mapper.VehicleMapper;
import control.prendario.vehicle.repository.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class VehicleService {

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Transactional
    public Vehicle saveVehicle(VehicleDTO vehicleDTO) {
        // Verificar si ya existe un vehículo con esa placa
        Optional<Vehicle> existingVehicle = vehicleRepository.findByPlate(vehicleDTO.getPlate());
        if (existingVehicle.isPresent()) {
            // Si existe, actualizamos sus datos
            Vehicle vehicle = existingVehicle.get();
            updateVehicleFromDTO(vehicle, vehicleDTO);
            vehicle.setActive(true);
            return vehicleRepository.save(vehicle);
        }

        // Si no existe, creamos uno nuevo
        Vehicle vehicle = vehicleMapper.toEntity(vehicleDTO);
        vehicle.setActive(true);
        return vehicleRepository.save(vehicle);
    }

    private boolean hasChanges(Vehicle vehicle, VehicleDTO dto) {
        return !Objects.equals(vehicle.getVehicleType().name(), dto.getVehicleType()) ||
                !Objects.equals(vehicle.getBrand(), dto.getBrand()) ||
                !Objects.equals(vehicle.getLine(), dto.getLine()) ||
                !Objects.equals(vehicle.getModel(), dto.getModel()) ||
                !Objects.equals(vehicle.getEngineDisplacement(), dto.getEngineDisplacement()) ||
                !Objects.equals(vehicle.getColor(), dto.getColor()) ||
                !Objects.equals(vehicle.getEngineNumber(), dto.getEngineNumber().toUpperCase()) ||
                !Objects.equals(vehicle.getChassisNumber(), dto.getChassisNumber().toUpperCase()) ||
                !Objects.equals(vehicle.getRegistrationPlace(), dto.getRegistrationPlace()) ||
                !Objects.equals(vehicle.getRegistrationDate(), dto.getRegistrationDate()) ||
                !Objects.equals(vehicle.getOwner(), dto.getOwner()) ||
                !Objects.equals(vehicle.getOwnerDocumentNumber(), dto.getOwnerDocumentNumber());
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .filter(vehicle -> !Boolean.FALSE.equals(vehicle.getActive()))
                .toList();
    }

    public Vehicle getVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehículo no encontrado"));
    }

    public void deleteVehicle(Long id) {
        Vehicle vehicle = getVehicleById(id);
        vehicle.setActive(false);
        vehicleRepository.save(vehicle);
    }

    public boolean existsByPlate(String plate) {
        return vehicleRepository.existsByPlate(plate);
    }

    @Transactional
    public Vehicle updateVehicle(VehicleDTO vehicleDTO) {
        if (vehicleDTO.getVehicleId() == null) {
            throw new RuntimeException("No se puede actualizar un vehículo sin ID");
        }

        Vehicle existingVehicle = vehicleRepository.findById(vehicleDTO.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehículo no encontrado"));

        // Si la placa está cambiando, verificar que no exista
        if (!existingVehicle.getPlate().equalsIgnoreCase(vehicleDTO.getPlate())) {
            vehicleRepository.findByPlate(vehicleDTO.getPlate())
                    .ifPresent(v -> {
                        throw new RuntimeException("Ya existe un vehículo con la placa: " + vehicleDTO.getPlate());
                    });
        }

        updateVehicleFromDTO(existingVehicle, vehicleDTO);
        existingVehicle.setActive(true);
        return vehicleRepository.save(existingVehicle);
    }

    private void updateVehicleFromDTO(Vehicle vehicle, VehicleDTO dto) {
        try {
            if (dto.getVehicleType() != null) {
                vehicle.setVehicleType(VehicleType.valueOf(dto.getVehicleType()));
            }
            if (dto.getBrand() != null) {
                vehicle.setBrand(dto.getBrand().toUpperCase());
            }
            if (dto.getLine() != null) {
                vehicle.setLine(dto.getLine().toUpperCase());
            }
            if (dto.getModel() != null) {
                vehicle.setModel(dto.getModel());
            }
            if (dto.getPlate() != null) {
                vehicle.setPlate(dto.getPlate().toUpperCase());
            }
            if (dto.getEngineDisplacement() != null) {
                vehicle.setEngineDisplacement(dto.getEngineDisplacement());
            }
            if (dto.getColor() != null) {
                vehicle.setColor(dto.getColor().toUpperCase());
            }
            if (dto.getEngineNumber() != null) {
                vehicle.setEngineNumber(dto.getEngineNumber().toUpperCase());
            }
            if (dto.getChassisNumber() != null) {
                vehicle.setChassisNumber(dto.getChassisNumber().toUpperCase());
            }
            if (dto.getRegistrationPlace() != null) {
                vehicle.setRegistrationPlace(dto.getRegistrationPlace().toUpperCase());
            }
            if (dto.getRegistrationDate() != null) {
                vehicle.setRegistrationDate(dto.getRegistrationDate());
            }
            if (dto.getOwner() != null) {
                vehicle.setOwner(dto.getOwner().toUpperCase());
            }
            if (dto.getOwnerDocumentNumber() != null) {
                vehicle.setOwnerDocumentNumber(dto.getOwnerDocumentNumber());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al actualizar vehículo: " + e.getMessage());
        }
    }

    public Optional<Vehicle> findByPlate(String plate) {
        return vehicleRepository.findByPlate(plate);
    }

}
