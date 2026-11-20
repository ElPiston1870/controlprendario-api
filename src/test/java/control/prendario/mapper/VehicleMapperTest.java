package control.prendario.mapper;

import control.prendario.DTO.VehicleDTO;
import control.prendario.model.TipoVehiculo;
import control.prendario.model.Vehicle;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class VehicleMapperTest {

    private final VehicleMapper mapper = new VehicleMapper();

    @Test
    void convierteDtoCompletoAEntidad() {
        VehicleDTO dto = dto();
        dto.setIdVehiculo(4L);

        Vehicle vehicle = mapper.toEntity(dto);

        assertEquals(4L, vehicle.getIdVehiculo());
        assertEquals(TipoVehiculo.CAMIONETA, vehicle.getTipoVehiculo());
        assertEquals(dto.getMarca(), vehicle.getMarca());
        assertEquals(dto.getLinea(), vehicle.getLinea());
        assertEquals(dto.getModelo(), vehicle.getModelo());
        assertEquals(dto.getPlaca(), vehicle.getPlaca());
        assertEquals(dto.getCilindraje(), vehicle.getCilindraje());
        assertEquals(dto.getColor(), vehicle.getColor());
        assertEquals(dto.getNumeroMotor(), vehicle.getNumeroMotor());
        assertEquals(dto.getNumeroChasis(), vehicle.getNumeroChasis());
        assertEquals(dto.getSitioMatricula(), vehicle.getSitioMatricula());
        assertEquals(dto.getFechaMatricula(), vehicle.getFechaMatricula());
        assertEquals(dto.getPropietario(), vehicle.getPropietario());
        assertEquals(dto.getNumeroDocumentoPropietario(), vehicle.getNumeroDocumentoPropietario());
    }

    @Test
    void convierteEntidadADtoYAdmiteIdNulo() {
        Vehicle vehicle = mapper.toEntity(dto());
        VehicleDTO dto = mapper.toDTO(vehicle);

        assertNull(vehicle.getIdVehiculo());
        assertEquals("CAMIONETA", dto.getTipoVehiculo());
        assertEquals(vehicle.getPlaca(), dto.getPlaca());
        assertEquals(vehicle.getFechaMatricula(), dto.getFechaMatricula());
        assertNull(mapper.toEntity(null));
        assertNull(mapper.toDTO(null));
    }

    private VehicleDTO dto() {
        VehicleDTO dto = new VehicleDTO();
        dto.setTipoVehiculo("CAMIONETA");
        dto.setMarca("Toyota");
        dto.setLinea("Hilux");
        dto.setModelo(2022);
        dto.setPlaca("ABC123");
        dto.setCilindraje(2400);
        dto.setColor("Blanco");
        dto.setNumeroMotor("MOTOR");
        dto.setNumeroChasis("CHASIS");
        dto.setSitioMatricula("Bogota");
        dto.setFechaMatricula(new Date(0));
        dto.setPropietario("Ana");
        dto.setNumeroDocumentoPropietario("123");
        return dto;
    }
}
