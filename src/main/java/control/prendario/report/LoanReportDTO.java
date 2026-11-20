package control.prendario.report;

import control.prendario.loan.Loan;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LoanReportDTO {
    @JsonProperty("cliente")
    private ClientReportDTO client;
    @JsonProperty("vehiculo")
    private VehicleReportDTO vehicle;
    @JsonProperty("montoPrestamo")
    private BigDecimal loanAmount;
    @JsonProperty("tasaInteres")
    private BigDecimal interestRate;
    @JsonProperty("fechaPrestamo")
    private LocalDateTime loanDate;
    @JsonProperty("fechaVencimiento")
    private LocalDateTime dueDate;

    public static LoanReportDTO fromLoan(Loan loan) {
        LoanReportDTO dto = new LoanReportDTO();
        dto.setLoanAmount(loan.getLoanAmount());
        dto.setInterestRate(loan.getInterestRate());
        dto.setLoanDate(loan.getLoanDate());
        dto.setDueDate(loan.getDueDate());

        if (loan.getClient() != null) {
            dto.setClient(ClientReportDTO.fromClient(loan.getClient()));
        }

        if (loan.getVehicle() != null) {
            dto.setVehicle(VehicleReportDTO.fromVehicle(loan.getVehicle()));
        }

        return dto;
    }
}
