package control.prendario.vehicle;

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
        when(service.getAllVehicles()).thenReturn(List.of(vehicle));
        when(service.getVehicleById(4L)).thenReturn(vehicle);
        when(service.updateVehicle(dto)).thenReturn(vehicle);

        assertSame(vehicle, controller.createVehicle(dto).getBody());
        assertEquals(List.of(vehicle), controller.getAllVehicles().getBody());
        assertSame(vehicle, controller.getVehicleById(4L).getBody());
        assertSame(vehicle, controller.updateVehicle(4L, dto).getBody());
        assertEquals(4L, dto.getVehicleId());
        assertEquals(HttpStatus.OK, controller.deleteVehicle(4L).getStatusCode());
    }

    @Test
    void returnsServiceErrorResponses() {
        VehicleDTO dto = new VehicleDTO();
        when(service.saveVehicle(dto)).thenThrow(new RuntimeException("duplicado"));
        when(service.getAllVehicles()).thenThrow(new RuntimeException("error"));
        when(service.getVehicleById(9L)).thenThrow(new RuntimeException("no existe"));
        when(service.updateVehicle(dto)).thenThrow(new RuntimeException("invalido"));
        doThrow(new RuntimeException("no existe")).when(service).deleteVehicle(9L);

        assertEquals(HttpStatus.BAD_REQUEST, controller.createVehicle(dto).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getAllVehicles().getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.getVehicleById(9L).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.updateVehicle(9L, dto).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.deleteVehicle(9L).getStatusCode());
    }
}
