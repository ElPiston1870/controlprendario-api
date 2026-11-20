package control.prendario.report.controller;

import control.prendario.report.service.ReportService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportControllerTest {

    private ReportService service;
    private ReportController controller;
    private final byte[] pdf = "%PDF-test".getBytes(java.nio.charset.StandardCharsets.US_ASCII);

    @BeforeEach
    void setUp() {
        service = mock(ReportService.class);
        controller = new ReportController(service);
    }

    @Test
    void returnsVehicleAndMachineryPdfDocuments() throws Exception {
        when(service.generatePledgeContract(1L)).thenReturn(pdf);
        when(service.generateMachineryPledgeContract(2L)).thenReturn(pdf);

        var vehicleResponse = controller.generatePledgeContract(1L);
        var machineryResponse = controller.generateMachineryPledgeContract(2L);

        assertEquals(HttpStatus.OK, vehicleResponse.getStatusCode());
        assertEquals(MediaType.APPLICATION_PDF, vehicleResponse.getHeaders().getContentType());
        assertEquals(pdf, vehicleResponse.getBody());
        assertEquals(HttpStatus.OK, machineryResponse.getStatusCode());
        assertEquals(MediaType.APPLICATION_PDF, machineryResponse.getHeaders().getContentType());
    }

    @Test
    void returnsInternalErrorWhenContractsCannotBeGenerated() throws Exception {
        when(service.generatePledgeContract(1L)).thenThrow(new Exception("error"));
        when(service.generateMachineryPledgeContract(2L)).thenThrow(new Exception("error"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.generatePledgeContract(1L).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.generateMachineryPledgeContract(2L).getStatusCode());
    }

    @Test
    void handlesVehicleReceiptsOnSuccessNotFoundAndInternalError() throws Exception {
        when(service.generatePaymentReceipt(1L)).thenReturn(pdf);
        when(service.generatePaymentReceipt(2L)).thenThrow(new EntityNotFoundException("no existe"));
        when(service.generatePaymentReceipt(3L)).thenThrow(new IllegalStateException("error"));

        var success = controller.generatePaymentReceipt(1L);
        assertEquals(HttpStatus.OK, success.getStatusCode());
        assertEquals(MediaType.APPLICATION_PDF, success.getHeaders().getContentType());
        assertEquals("attachment; filename=\"recibo_pago_1.pdf\"",
                success.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(HttpStatus.NOT_FOUND, controller.generatePaymentReceipt(2L).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.generatePaymentReceipt(3L).getStatusCode());
    }

    @Test
    void handlesMachineryReceiptsOnSuccessNotFoundAndInternalError() throws Exception {
        when(service.generateMachineryPaymentReceipt(1L)).thenReturn(pdf);
        when(service.generateMachineryPaymentReceipt(2L)).thenThrow(new EntityNotFoundException("no existe"));
        when(service.generateMachineryPaymentReceipt(3L)).thenThrow(new IllegalStateException("error"));

        var success = controller.generateMachineryPaymentReceipt(1L);
        assertEquals(HttpStatus.OK, success.getStatusCode());
        assertEquals("attachment; filename=\"recibo_pago_maquinaria_1.pdf\"",
                success.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(HttpStatus.NOT_FOUND, controller.generateMachineryPaymentReceipt(2L).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                controller.generateMachineryPaymentReceipt(3L).getStatusCode());
    }

    @Test
    void generatesSummaryPdfOrReturnsInternalError() throws Exception {
        when(service.generateLoanSummaryPdf()).thenReturn(pdf);
        var response = controller.generateLoanSummaryPdf();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertEquals("attachment; filename=\"resumen_prestamos.pdf\"",
                response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));

        when(service.generateLoanSummaryPdf()).thenThrow(new Exception("error"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.generateLoanSummaryPdf().getStatusCode());
    }
}
