package control.prendario.vehicle.controller;

import control.prendario.shared.query.PageResult;
import control.prendario.vehicle.domain.Vehicle;
import control.prendario.vehicle.dto.VehicleDTO;
import control.prendario.vehicle.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VehicleControllerTest {

    private VehicleService service;
    private VehicleController controller;

    @BeforeEach
    void setUp() {
        service = mock(VehicleService.class);
        controller = new VehicleController(service);
    }

    @Test
    void delegatesSuccessfulCrudOperations() {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleId(4L);
        VehicleDTO dto = new VehicleDTO();
        when(service.saveVehicle(dto)).thenReturn(vehicle);
        var page = new PageResult<>(List.of(vehicle), 0, 20, 1, 1);
        when(service.pageVehicles(null)).thenReturn(page);
        when(service.getVehicleById(4L)).thenReturn(vehicle);
        when(service.updateVehicle(dto)).thenReturn(vehicle);

        assertSame(vehicle, controller.createVehicle(dto).getBody());
        assertEquals(page, controller.getAllVehicles(null).getBody());
        assertSame(vehicle, controller.getVehicleById(4L).getBody());
        assertSame(vehicle, controller.updateVehicle(4L, dto).getBody());
        assertEquals(4L, dto.getVehicleId());
        assertEquals(HttpStatus.OK, controller.deleteVehicle(4L).getStatusCode());
    }

    @Test
    void returnsServiceErrorResponses() {
        VehicleDTO dto = new VehicleDTO();
        when(service.saveVehicle(dto)).thenThrow(new RuntimeException("duplicado"));
        when(service.getVehicleById(9L)).thenThrow(new RuntimeException("no existe"));
        when(service.updateVehicle(dto)).thenThrow(new RuntimeException("invalido"));
        doThrow(new RuntimeException("no existe")).when(service).deleteVehicle(9L);

        assertEquals(HttpStatus.BAD_REQUEST, controller.createVehicle(dto).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.getVehicleById(9L).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.updateVehicle(9L, dto).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.deleteVehicle(9L).getStatusCode());
    }
}
