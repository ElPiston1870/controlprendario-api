package control.prendario.service;

import control.prendario.repository.PagoMaquinaRepository;
import control.prendario.repository.PagoRepository;
import control.prendario.repository.PrestamoMaquinaRepository;
import control.prendario.repository.PrestamoRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ReporteResumenTest {

    @Test
    void generaPdfDeResumenInclusoSinPrestamos() throws Exception {
        ReporteService reporteService = new ReporteService(
                mock(PrestamoRepository.class),
                mock(PrestamoMaquinaRepository.class),
                mock(PagoRepository.class),
                mock(PagoMaquinaRepository.class)
        );

        byte[] pdf = reporteService.generarResumenPrestamosPdf();

        assertTrue(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII).startsWith("%PDF-"));
    }
}
