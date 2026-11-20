package control.prendario.report;

import control.prendario.client.Client;
import control.prendario.loan.Loan;
import control.prendario.loan.LoanRepository;
import control.prendario.loan.LoanStatus;
import control.prendario.loan.MachineryLoan;
import control.prendario.loan.MachineryLoanRepository;
import control.prendario.payment.MachineryPayment;
import control.prendario.payment.MachineryPaymentRepository;
import control.prendario.payment.Payment;
import control.prendario.payment.PaymentRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static control.prendario.report.NumberToLetterConverter.convertNumberToLetter;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final LoanRepository loanRepository;
    private final MachineryLoanRepository machineryLoanRepository;
    private final PaymentRepository paymentRepository;
    private final MachineryPaymentRepository machineryPaymentRepository;

    public byte[] generatePledgeContract(Long loanId) throws Exception {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        // Crear DTO plano para el reporte
        Map<String, Object> reportData = new HashMap<>();

        // Cliente
        Map<String, Object> client = new HashMap<>();
        client.put("nombres", loan.getClient().getFirstNames());
        client.put("apellidos", loan.getClient().getLastNames());
        client.put("ciudad", loan.getClient().getCity());
        client.put("numeroDocumento", loan.getClient().getDocumentNumber());
        reportData.put("cliente", client);

        // Vehículo
        Map<String, Object> vehicle = new HashMap<>();
        vehicle.put("tipoVehiculo", loan.getVehicle().getVehicleType().toString());
        vehicle.put("marca", loan.getVehicle().getBrand());
        vehicle.put("linea", loan.getVehicle().getLine());
        vehicle.put("modelo", loan.getVehicle().getModel());
        vehicle.put("placa", loan.getVehicle().getPlate());
        vehicle.put("cilindraje", loan.getVehicle().getEngineDisplacement());
        vehicle.put("color", loan.getVehicle().getColor());
        vehicle.put("numeroMotor", loan.getVehicle().getEngineNumber());
        vehicle.put("numeroChasis", loan.getVehicle().getChassisNumber());
        vehicle.put("sitioMatricula", loan.getVehicle().getRegistrationPlace());
        vehicle.put("fechaMatricula", loan.getVehicle().getRegistrationDate());
        vehicle.put("propietario", loan.getVehicle().getOwner());
        reportData.put("vehiculo", vehicle);

        // Datos del préstamo
        reportData.put("montoPrestamo", loan.getLoanAmount());
        reportData.put("tasaInteres", loan.getInterestRate());
        reportData.put("fechaPrestamo", loan.getLoanDate());
        reportData.put("fechaVencimiento", loan.getDueDate());

        // Parámetros
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("IMAGEN", new ClassPathResource("reportes/2.png").getInputStream());
        parameters.put("MONTO_LETRAS", convertNumberToLetter(loan.getLoanAmount().doubleValue()));

        // Generar reporte
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(reportData));

        InputStream reportStream = new ClassPathResource("reportes/documento_empeno.jasper").getInputStream();
        JasperPrint jasperPrint = JasperFillManager.fillReport(reportStream, parameters, dataSource);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JRPdfExporter exporter = new JRPdfExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));

        SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
        exporter.setConfiguration(configuration);

        exporter.exportReport();

        return baos.toByteArray();
    }

    public byte[] generateMachineryPledgeContract(Long machineryLoanId) throws Exception {
        MachineryLoan loan = machineryLoanRepository.findById(machineryLoanId)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        // Crear DTO plano para el reporte
        Map<String, Object> reportData = new HashMap<>();

        // Cliente
        Map<String, Object> client = new HashMap<>();
        client.put("nombres", loan.getClient().getFirstNames());
        client.put("apellidos", loan.getClient().getLastNames());
        client.put("ciudad", loan.getClient().getCity());
        client.put("numeroDocumento", loan.getClient().getDocumentNumber());
        reportData.put("cliente", client);

        // Maquina
        reportData.put("tipoMaquina", loan.getMachineType());
        reportData.put("marcaMaquina", loan.getMachineBrand());

        // Datos del préstamo
        reportData.put("montoPrestamo", loan.getLoanAmount());
        reportData.put("tasaInteres", loan.getInterestRate());
        reportData.put("fechaPrestamo", loan.getLoanDate());
        reportData.put("fechaVencimiento", loan.getDueDate());

        // Parámetros
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("IMAGEN", new ClassPathResource("reportes/2.png").getInputStream());
        parameters.put("MONTO_LETRAS", convertNumberToLetter(loan.getLoanAmount().doubleValue()));

        // Generar reporte
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(reportData));

        InputStream reportStream = new ClassPathResource("reportes/documento_maquina.jasper").getInputStream();
        JasperPrint jasperPrint = JasperFillManager.fillReport(reportStream, parameters, dataSource);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JRPdfExporter exporter = new JRPdfExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));

        SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
        exporter.setConfiguration(configuration);

        exporter.exportReport();

        return baos.toByteArray();
    }

    @Transactional(readOnly = true)
    public byte[] generateLoanSummaryPdf() throws Exception {
        List<Loan> vehicleLoans = loanRepository.findAll().stream()
                .filter(loan -> !Boolean.FALSE.equals(loan.getActive()))
                .toList();
        List<MachineryLoan> machineryLoans = machineryLoanRepository.findAll().stream()
                .filter(loan -> !Boolean.FALSE.equals(loan.getActive()))
                .toList();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 28, 28, 28, 28);
        PdfWriter.getInstance(document, output);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new BaseColor(31, 78, 121));
        Font summaryFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.DARK_GRAY);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.WHITE);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK);

        Paragraph title = new Paragraph("RESUMEN DE PRÉSTAMOS", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        Paragraph generatedAt = new Paragraph(
                "Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                summaryFont);
        generatedAt.setAlignment(Element.ALIGN_RIGHT);
        generatedAt.setSpacingAfter(12);
        document.add(generatedAt);

        int count = 0;
        BigDecimal totalLent = BigDecimal.ZERO;
        BigDecimal totalPendingPrincipal = BigDecimal.ZERO;
        BigDecimal totalPendingInterest = BigDecimal.ZERO;

        PdfPTable table = new PdfPTable(new float[]{0.55f, 0.8f, 1.65f, 1.7f, 1.1f, 1.1f, 0.9f, 1.0f});
        table.setWidthPercentage(100);
        table.setHeaderRows(1);
        for (String heading : List.of("ID", "TIPO", "CLIENTE", "GARANTÍA", "PRÉSTAMO",
                "CAPITAL PEND.", "INTERÉS PEND.", "ESTADO / VENCE")) {
            PdfPCell cell = new PdfPCell(new Phrase(heading, headerFont));
            cell.setBackgroundColor(new BaseColor(31, 78, 121));
            cell.setPadding(5);
            table.addCell(cell);
        }

        for (Loan loan : vehicleLoans) {
            BigDecimal amount = zeroIfNull(loan.getLoanAmount());
            BigDecimal capitalPending = loan.getPendingPrincipal() != null
                    ? loan.getPendingPrincipal()
                    : amount.subtract(zeroIfNull(loan.getPaidPrincipal())).max(BigDecimal.ZERO);
            BigDecimal interestPending = zeroIfNull(loan.getPendingInterest());
            String guarantee = loan.getVehicle() == null ? "Sin vehículo"
                    : value(loan.getVehicle().getBrand()) + " "
                    + value(loan.getVehicle().getLine()) + " "
                    + value(loan.getVehicle().getPlate());
            addSummaryRow(table, loan.getLoanId(), "Vehicular",
                    clientName(loan.getClient()), guarantee, amount, capitalPending,
                    interestPending, loan.getLoanStatus(), loan.getDueDate(), cellFont);
            count++;
            totalLent = totalLent.add(amount);
            totalPendingPrincipal = totalPendingPrincipal.add(capitalPending);
            totalPendingInterest = totalPendingInterest.add(interestPending);
        }

        for (MachineryLoan loan : machineryLoans) {
            BigDecimal amount = zeroIfNull(loan.getLoanAmount());
            BigDecimal capitalPending = loan.getPendingPrincipal() != null
                    ? loan.getPendingPrincipal()
                    : amount.subtract(zeroIfNull(loan.getPaidPrincipal())).max(BigDecimal.ZERO);
            BigDecimal interestPending = zeroIfNull(loan.getPendingInterest());
            String guarantee = value(loan.getMachineType()) + " " + value(loan.getMachineBrand());
            addSummaryRow(table, loan.getMachineryLoanId(), "Maquinaria",
                    clientName(loan.getClient()), guarantee, amount, capitalPending,
                    interestPending, loan.getLoanStatus(), loan.getDueDate(), cellFont);
            count++;
            totalLent = totalLent.add(amount);
            totalPendingPrincipal = totalPendingPrincipal.add(capitalPending);
            totalPendingInterest = totalPendingInterest.add(interestPending);
        }

        if (count == 0) {
            PdfPCell empty = new PdfPCell(new Phrase("No hay préstamos registrados.", cellFont));
            empty.setColspan(8);
            empty.setHorizontalAlignment(Element.ALIGN_CENTER);
            empty.setPadding(10);
            table.addCell(empty);
        }
        document.add(table);

        Paragraph totals = new Paragraph(
                String.format("Préstamos: %d    Total prestado: %s    Capital pendiente: %s    Interés pendiente: %s",
                        count, formatAmount(totalLent), formatAmount(totalPendingPrincipal),
                        formatAmount(totalPendingInterest)),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new BaseColor(31, 78, 121)));
        totals.setSpacingBefore(12);
        document.add(totals);
        document.close();
        return output.toByteArray();
    }

    private void addSummaryRow(
            PdfPTable table,
            Long loanId,
            String type,
            String client,
            String collateral,
            BigDecimal amount,
            BigDecimal pendingPrincipal,
            BigDecimal pendingInterest,
            LoanStatus status,
            Object dueDateValue,
            Font font
    ) {
        List<String> values = List.of(
                value(loanId),
                type,
                client,
                collateral,
                formatAmount(amount),
                formatAmount(pendingPrincipal),
                formatAmount(pendingInterest),
                value(status) + " / " + value(dueDateValue)
        );
        for (String value : values) {
            PdfPCell cell = new PdfPCell(new Phrase(value, font));
            cell.setPadding(4);
            table.addCell(cell);
        }
    }

    private String clientName(Client client) {
        return client == null ? "No registrado"
                : value(client.getFirstNames()) + " " + value(client.getLastNames());
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    @Transactional(readOnly = true)
    public byte[] generatePaymentReceipt(Long paymentId) throws Exception {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado"));
        Loan loan = payment.getLoan();
        Client client = loan != null ? loan.getClient() : null;
        String collateral = loan != null && loan.getVehicle() != null
                ? String.format("%s %s %s (placa %s)",
                value(loan.getVehicle().getBrand()),
                value(loan.getVehicle().getLine()),
                value(loan.getVehicle().getModel()),
                value(loan.getVehicle().getPlate()))
                : "No registrada";

        return createReceiptPdf(
                payment.getPaymentId(),
                payment.getPaymentDate(),
                payment.getAmountPaid(),
                payment.getPaymentType() != null ? payment.getPaymentType().name() : null,
                payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null,
                payment.getNotes(),
                loan != null ? loan.getLoanId() : null,
                loan != null && loan.getLoanStatus() != null
                        ? loan.getLoanStatus().name() : null,
                client,
                "Vehículo: " + collateral
        );
    }

    @Transactional(readOnly = true)
    public byte[] generateMachineryPaymentReceipt(Long machineryPaymentId) throws Exception {
        MachineryPayment payment = machineryPaymentRepository.findById(machineryPaymentId)
                .orElseThrow(() -> new EntityNotFoundException("Pago de maquinaria no encontrado"));
        MachineryLoan loan = payment.getMachineryLoan();
        Client client = loan != null ? loan.getClient() : null;
        String collateral = loan != null
                ? String.format("%s %s",
                value(loan.getMachineType()), value(loan.getMachineBrand()))
                : "No registrada";

        return createReceiptPdf(
                payment.getMachineryPaymentId(),
                payment.getPaymentDate(),
                payment.getAmountPaid(),
                payment.getPaymentType() != null ? payment.getPaymentType().name() : null,
                payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null,
                payment.getNotes(),
                loan != null ? loan.getMachineryLoanId() : null,
                loan != null && loan.getLoanStatus() != null
                        ? loan.getLoanStatus().name() : null,
                client,
                "Maquinaria: " + collateral
        );
    }

    private byte[] createReceiptPdf(
            Long paymentId,
            java.time.LocalDateTime paymentDate,
            BigDecimal amountPaid,
            String paymentType,
            String paymentMethod,
            String notes,
            Long loanId,
            String loanStatus,
            Client client,
            String collateral
    ) throws DocumentException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A5, 42, 42, 36, 36);
        PdfWriter.getInstance(document, output);
        document.open();

        Font titleText = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new BaseColor(31, 78, 121));
        Font subtitle = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.DARK_GRAY);
        Font label = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.DARK_GRAY);
        Font value = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
        Font amount = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new BaseColor(31, 78, 121));

        Paragraph headerText = new Paragraph("RECIBO DE PAGO", titleText);
        headerText.setAlignment(Element.ALIGN_CENTER);
        document.add(headerText);

        Paragraph reference = new Paragraph("Comprobante No. " + paymentId, subtitle);
        reference.setAlignment(Element.ALIGN_CENTER);
        reference.setSpacingAfter(16);
        document.add(reference);

        PdfPTable pdfTable = new PdfPTable(new float[]{1.1f, 2f});
        pdfTable.setWidthPercentage(100);
        pdfTable.setSpacingAfter(14);
        addRow(pdfTable, "Fecha y hora", paymentDate == null
                ? "No registrada"
                : paymentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), label, value);
        addRow(pdfTable, "Cliente", client == null ? "No registrado"
                : value(client.getFirstNames()) + " " + value(client.getLastNames()), label, value);
        addRow(pdfTable, "Documento", client == null ? "No registrado"
                : value(client.getDocumentNumber()), label, value);
        addRow(pdfTable, "Préstamo", loanId == null ? "No registrado" : loanId.toString(), label, value);
        addRow(pdfTable, "Estado del préstamo", value(loanStatus), label, value);
        addRow(pdfTable, "Concepto", value(paymentType), label, value);
        addRow(pdfTable, "Método de pago", value(paymentMethod), label, value);
        addRow(pdfTable, "Garantía", collateral, label, value);
        addRow(pdfTable, "Observaciones", value(notes), label, value);
        document.add(pdfTable);

        Paragraph amountReceived = new Paragraph("Monto recibido: " + formatAmount(amountPaid), amount);
        amountReceived.setAlignment(Element.ALIGN_RIGHT);
        amountReceived.setSpacingBefore(8);
        document.add(amountReceived);

        Paragraph footer = new Paragraph("Este documento certifica el registro del pago indicado.", subtitle);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(22);
        document.add(footer);

        document.close();
        return output.toByteArray();
    }

    private void addRow(PdfPTable pdfTable, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(PdfPCell.NO_BORDER);
        labelCell.setPadding(5);
        pdfTable.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(PdfPCell.NO_BORDER);
        valueCell.setPadding(5);
        pdfTable.addCell(valueCell);
    }

    private String value(Object text) {
        return text == null || text.toString().isBlank() ? "No registrado" : text.toString();
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "No registrado";
        }
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));
        numberFormat.setCurrency(Currency.getInstance("COP"));
        return numberFormat.format(amount);
    }
}