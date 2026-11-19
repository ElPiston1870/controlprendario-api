package control.prendario.service;

import control.prendario.DTO.GarantiaInventarioDTO;
import control.prendario.model.EstadoPrestamo;
import control.prendario.model.Prestamo;
import control.prendario.model.PrestamoMaquina;
import control.prendario.repository.PrestamoMaquinaRepository;
import control.prendario.repository.PrestamoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GarantiaInventarioService {

    private final PrestamoRepository prestamoRepository;
    private final PrestamoMaquinaRepository prestamoMaquinaRepository;

    @Transactional(readOnly = true)
    public List<GarantiaInventarioDTO> consultarInventario() {
        List<GarantiaInventarioDTO> inventario = new ArrayList<>();

        for (Prestamo prestamo : prestamoRepository.findAll()) {
            if (Boolean.FALSE.equals(prestamo.getActivo())) {
                continue;
            }
            String garantia = prestamo.getVehiculo() == null
                    ? "Sin vehículo asociado"
                    : String.format("%s %s %s %s",
                    valor(prestamo.getVehiculo().getTipoVehiculo()),
                    valor(prestamo.getVehiculo().getMarca()),
                    valor(prestamo.getVehiculo().getLinea()),
                    valor(prestamo.getVehiculo().getModelo()));
            String placa = prestamo.getVehiculo() == null
                    ? "No registrada" : valor(prestamo.getVehiculo().getPlaca());
            inventario.add(GarantiaInventarioDTO.builder()
                    .tipoGarantia("VEHICULO")
                    .idPrestamo(prestamo.getIdPrestamo())
                    .idCliente(prestamo.getCliente() == null ? null : prestamo.getCliente().getIdCliente())
                    .nombreCliente(nombreCliente(prestamo))
                    .identificador(placa)
                    .descripcion(garantia)
                    .montoPrestamo(prestamo.getMontoPrestamo())
                    .capitalPendiente(valorMonto(prestamo.getCapitalPendiente(), prestamo.getMontoPrestamo()))
                    .estadoPrestamo(valor(prestamo.getEstadoPrestamo()))
                    .fechaPrestamo(prestamo.getFechaPrestamo())
                    .fechaVencimiento(valor(prestamo.getFechaVencimiento()))
                    .enGarantia(!esPrestamoSaldado(prestamo.getEstadoPrestamo()))
                    .build());
        }

        for (PrestamoMaquina prestamo : prestamoMaquinaRepository.findAll()) {
            if (Boolean.FALSE.equals(prestamo.getActivo())) {
                continue;
            }
            inventario.add(GarantiaInventarioDTO.builder()
                    .tipoGarantia("MAQUINARIA")
                    .idPrestamo(prestamo.getIdPrestamoMaquina())
                    .idCliente(prestamo.getCliente() == null ? null : prestamo.getCliente().getIdCliente())
                    .nombreCliente(nombreCliente(prestamo))
                    .identificador(valor(prestamo.getTipoMaquina()) + " " + valor(prestamo.getMarcaMaquina()))
                    .descripcion("Maquinaria en garantía")
                    .montoPrestamo(prestamo.getMontoPrestamo())
                    .capitalPendiente(valorMonto(prestamo.getCapitalPendiente(), prestamo.getMontoPrestamo()))
                    .estadoPrestamo(valor(prestamo.getEstadoPrestamo()))
                    .fechaPrestamo(prestamo.getFechaPrestamo())
                    .fechaVencimiento(valor(prestamo.getFechaVencimiento()))
                    .enGarantia(!esPrestamoSaldado(prestamo.getEstadoPrestamo()))
                    .build());
        }

        return inventario;
    }

    private String nombreCliente(Prestamo prestamo) {
        if (prestamo.getCliente() == null) {
            return "Cliente no registrado";
        }
        return valor(prestamo.getCliente().getNombres()) + " " + valor(prestamo.getCliente().getApellidos());
    }

    private String nombreCliente(PrestamoMaquina prestamo) {
        if (prestamo.getCliente() == null) {
            return "Cliente no registrado";
        }
        return valor(prestamo.getCliente().getNombres()) + " " + valor(prestamo.getCliente().getApellidos());
    }

    private boolean esPrestamoSaldado(EstadoPrestamo estado) {
        return estado == EstadoPrestamo.PAGADO;
    }

    private BigDecimal valorMonto(BigDecimal monto, BigDecimal respaldo) {
        return monto != null ? monto : (respaldo != null ? respaldo : BigDecimal.ZERO);
    }

    private String valor(Object valor) {
        return valor == null || valor.toString().isBlank() ? "No registrado" : valor.toString();
    }
}
