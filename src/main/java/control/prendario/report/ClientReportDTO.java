package control.prendario.report;

import control.prendario.client.Client;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO para reportes de cliente")
@Data
public class ClientReportDTO {
    @Schema(description = "Nombres del cliente", example = "Juan Carlos")
    @JsonProperty("nombres")
    private String firstNames;

    @Schema(description = "Apellidos del cliente", example = "Pérez González")
    @JsonProperty("apellidos")
    private String lastNames;

    @Schema(description = "Número de documento del cliente", example = "12345678")
    @JsonProperty("numeroDocumento")
    private String documentNumber;

    @Schema(description = "Ciudad de residencia del cliente", example = "Bogotá")
    @JsonProperty("ciudad")
    private String city;

    public static ClientReportDTO fromClient(Client client) {
        ClientReportDTO dto = new ClientReportDTO();
        dto.setFirstNames(client.getFirstNames());
        dto.setLastNames(client.getLastNames());
        dto.setDocumentNumber(client.getDocumentNumber());
        dto.setCity(client.getCity());
        return dto;
    }
}