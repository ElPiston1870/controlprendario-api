package control.prendario.DTO;

import control.prendario.model.Cliente;
import control.prendario.model.Pago;
import control.prendario.model.PagoMaquina;
import control.prendario.model.Prestamo;
import control.prendario.model.PrestamoMaquina;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "Historial consolidado de préstamos y pagos de un cliente")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialClienteDTO {

    @Schema(description = "Datos generales del cliente")
    private Cliente cliente;

    @Schema(description = "Préstamos vehiculares asociados al cliente")
    private List<Prestamo> prestamosVehiculares;

    @Schema(description = "Préstamos de maquinaria asociados al cliente")
    private List<PrestamoMaquina> prestamosMaquinaria;

    @Schema(description = "Pagos realizados sobre préstamos vehiculares")
    private List<Pago> pagosVehiculares;

    @Schema(description = "Pagos realizados sobre préstamos de maquinaria")
    private List<PagoMaquina> pagosMaquinaria;
}
