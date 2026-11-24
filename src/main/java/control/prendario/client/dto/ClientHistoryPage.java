package control.prendario.client.dto;

import control.prendario.client.domain.Client;
import control.prendario.loan.domain.Loan;
import control.prendario.loan.domain.MachineryLoan;
import control.prendario.payment.domain.Payment;
import control.prendario.payment.domain.MachineryPayment;
import control.prendario.shared.query.PageResult;

public record ClientHistoryPage(Client cliente, PageResult<Loan> prestamosVehiculares,
        PageResult<MachineryLoan> prestamosMaquinaria, PageResult<Payment> pagosVehiculares,
        PageResult<MachineryPayment> pagosMaquinaria) {}
