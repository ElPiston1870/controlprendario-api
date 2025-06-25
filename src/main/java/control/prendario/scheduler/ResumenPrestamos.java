package control.prendario.scheduler;


import control.prendario.model.Prestamo;
import control.prendario.model.PrestamoMaquina;
import control.prendario.service.PagoMaquinaService;
import control.prendario.service.PagoService;
import control.prendario.service.PrestamoMaquinaService;
import control.prendario.service.PrestamoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResumenPrestamos {

    @Autowired
    private PrestamoService prestamoService;
    @Autowired
    private PagoService pagoService;

    @Autowired
    private PrestamoMaquinaService prestamoMaquinaService;
    @Autowired
    private PagoMaquinaService pagoMaquinaService;


    @Scheduled(cron = "0 0 1 * * *", zone = "America/Bogota")
    public void realizarResumen() {

        List<Prestamo> prestamos = prestamoService.getAllPrestamos();
        List<PrestamoMaquina> maquinas = prestamoMaquinaService.obtenerTodosPrestamosMaquina();

        for (Prestamo prestamo : prestamos){
            pagoService.obtenerResumenPagos(prestamo.getIdPrestamo());
        }

        for(PrestamoMaquina maquina: maquinas){
            pagoMaquinaService.obtenerResumenPagos(maquina.getIdPrestamoMaquina());
        }

        prestamos = null;
        maquinas = null;
    }
}
