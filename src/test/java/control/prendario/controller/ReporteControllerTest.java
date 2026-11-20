package control.prendario.controller;

import control.prendario.service.ReporteService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReporteControllerTest {

    private ReporteService service;
    private ReporteController controller;
    private final byte[] pdf = "%PDF-test".getBytes(java.nio.charset.StandardCharsets.US_ASCII);

    @BeforeEach
    void setUp() {
        service = mock(ReporteService.class);
        controller = new ReporteController(service);
    }

    @Test
    void devuelveDocumentosPdfVehicularYDeMaquinaria() throws Exception {
        when(service.generarDocumentoEmpeno(1L)).thenReturn(pdf);
        when(service.generarDocumentoEmpenoMaquina(2L)).thenReturn(pdf);

        var vehicular = controller.generarDocumentoEmpeno(1L);
        var maquinaria = controller.generarDocumentoEmpenoMaquina(2L);

        assertEquals(HttpStatus.OK, vehicular.getStatusCode());
        assertEquals(MediaType.APPLICATION_PDF, vehicular.getHeaders().getContentType());
        assertEquals(pdf, vehicular.getBody());
        assertEquals(HttpStatus.OK, maquinaria.getStatusCode());
        assertEquals(MediaType.APPLICATION_PDF, maquinaria.getHeaders().getContentType());
    }

    @Test
    void respondeErrorInternoSiNoPuedeGenerarLosContratos() throws Exception {
        when(service.generarDocumentoEmpeno(1L)).thenThrow(new Exception("error"));
        when(service.generarDocumentoEmpenoMaquina(2L)).thenThrow(new Exception("error"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.generarDocumentoEmpeno(1L).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.generarDocumentoEmpenoMaquina(2L).getStatusCode());
    }

    @Test
    void manejaRecibosVehicularesEnExitoNoEncontradoYErrorInterno() throws Exception {
        when(service.generarReciboPago(1L)).thenReturn(pdf);
        when(service.generarReciboPago(2L)).thenThrow(new EntityNotFoundException("no existe"));
        when(service.generarReciboPago(3L)).thenThrow(new IllegalStateException("error"));

        var success = controller.generarReciboPago(1L);
        assertEquals(HttpStatus.OK, success.getStatusCode());
        assertEquals(MediaType.APPLICATION_PDF, success.getHeaders().getContentType());
        assertEquals("attachment; filename=\"recibo_pago_1.pdf\"",
                success.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(HttpStatus.NOT_FOUND, controller.generarReciboPago(2L).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.generarReciboPago(3L).getStatusCode());
    }

    @Test
    void manejaRecibosDeMaquinariaEnExitoNoEncontradoYErrorInterno() throws Exception {
        when(service.generarReciboPagoMaquina(1L)).thenReturn(pdf);
        when(service.generarReciboPagoMaquina(2L)).thenThrow(new EntityNotFoundException("no existe"));
        when(service.generarReciboPagoMaquina(3L)).thenThrow(new IllegalStateException("error"));

        var success = controller.generarReciboPagoMaquina(1L);
        assertEquals(HttpStatus.OK, success.getStatusCode());
        assertEquals("attachment; filename=\"recibo_pago_maquinaria_1.pdf\"",
                success.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(HttpStatus.NOT_FOUND, controller.generarReciboPagoMaquina(2L).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                controller.generarReciboPagoMaquina(3L).getStatusCode());
    }

    @Test
    void generaResumenPdfODevuelveErrorInterno() throws Exception {
        when(service.generarResumenPrestamosPdf()).thenReturn(pdf);
        var response = controller.generarResumenPrestamosPdf();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertEquals("attachment; filename=\"resumen_prestamos.pdf\"",
                response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));

        when(service.generarResumenPrestamosPdf()).thenThrow(new Exception("error"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.generarResumenPrestamosPdf().getStatusCode());
    }
}
