package control.prendario.service;

import control.prendario.model.Cliente;
import control.prendario.model.Pago;
import control.prendario.model.PagoMaquina;
import control.prendario.model.Prestamo;
import control.prendario.model.PrestamoMaquina;
import control.prendario.model.EstadoPrestamo;
import control.prendario.repository.PagoMaquinaRepository;
import control.prendario.repository.PagoRepository;
import control.prendario.repository.PrestamoMaquinaRepository;
import control.prendario.repository.PrestamoRepository;
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

import static control.prendario.DTO.NumberToLetterConverter.convertNumberToLetter;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final PrestamoRepository prestamoRepository;
    private final PrestamoMaquinaRepository prestamoMaquinaRepository;
    private final PagoRepository pagoRepository;
    private final PagoMaquinaRepository pagoMaquinaRepository;

    public byte[] generarDocumentoEmpeno(Long idPrestamo) throws Exception {
        Prestamo prestamo = prestamoRepository.findById(idPrestamo)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        // Crear DTO plano para el reporte
        Map<String, Object> reportData = new HashMap<>();

        // Cliente
        Map<String, Object> cliente = new HashMap<>();
        cliente.put("nombres", prestamo.getCliente().getNombres());
        cliente.put("apellidos", prestamo.getCliente().getApellidos());
        cliente.put("ciudad", prestamo.getCliente().getCiudad());
        cliente.put("numeroDocumento", prestamo.getCliente().getNumeroDocumento());
        reportData.put("cliente", cliente);

        // Vehículo
        Map<String, Object> vehiculo = new HashMap<>();
        vehiculo.put("tipoVehiculo", prestamo.getVehiculo().getTipoVehiculo().toString());
        vehiculo.put("marca", prestamo.getVehiculo().getMarca());
        vehiculo.put("linea", prestamo.getVehiculo().getLinea());
        vehiculo.put("modelo", prestamo.getVehiculo().getModelo());
        vehiculo.put("placa", prestamo.getVehiculo().getPlaca());
        vehiculo.put("cilindraje", prestamo.getVehiculo().getCilindraje());
        vehiculo.put("color", prestamo.getVehiculo().getColor());
        vehiculo.put("numeroMotor", prestamo.getVehiculo().getNumeroMotor());
        vehiculo.put("numeroChasis", prestamo.getVehiculo().getNumeroChasis());
        vehiculo.put("sitioMatricula", prestamo.getVehiculo().getSitioMatricula());
        vehiculo.put("fechaMatricula", prestamo.getVehiculo().getFechaMatricula());
        vehiculo.put("propietario", prestamo.getVehiculo().getPropietario());
        reportData.put("vehiculo", vehiculo);

        // Datos del préstamo
        reportData.put("montoPrestamo", prestamo.getMontoPrestamo());
        reportData.put("tasaInteres", prestamo.getTasaInteres());
        reportData.put("fechaPrestamo", prestamo.getFechaPrestamo());
        reportData.put("fechaVencimiento", prestamo.getFechaVencimiento());

        // Parámetros
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("IMAGEN", new ClassPathResource("reportes/2.png").getInputStream());
        parameters.put("MONTO_LETRAS", convertNumberToLetter(prestamo.getMontoPrestamo().doubleValue()));

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

    public byte[] generarDocumentoEmpenoMaquina(Long idPrestamoMaquina) throws Exception {
        PrestamoMaquina prestamo = prestamoMaquinaRepository.findById(idPrestamoMaquina)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        // Crear DTO plano para el reporte
        Map<String, Object> reportData = new HashMap<>();

        // Cliente
        Map<String, Object> cliente = new HashMap<>();
        cliente.put("nombres", prestamo.getCliente().getNombres());
        cliente.put("apellidos", prestamo.getCliente().getApellidos());
        cliente.put("ciudad", prestamo.getCliente().getCiudad());
        cliente.put("numeroDocumento", prestamo.getCliente().getNumeroDocumento());
        reportData.put("cliente", cliente);

        // Maquina
        reportData.put("tipoMaquina", prestamo.getTipoMaquina());
        reportData.put("marcaMaquina", prestamo.getMarcaMaquina());

        // Datos del préstamo
        reportData.put("montoPrestamo", prestamo.getMontoPrestamo());
        reportData.put("tasaInteres", prestamo.getTasaInteres());
        reportData.put("fechaPrestamo", prestamo.getFechaPrestamo());
        reportData.put("fechaVencimiento", prestamo.getFechaVencimiento());

        // Parámetros
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("IMAGEN", new ClassPathResource("reportes/2.png").getInputStream());
        parameters.put("MONTO_LETRAS", convertNumberToLetter(prestamo.getMontoPrestamo().doubleValue()));

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
    public byte[] generarResumenPrestamosPdf() throws Exception {
        List<Prestamo> prestamosVehiculares = prestamoRepository.findAll().stream()
                .filter(prestamo -> !Boolean.FALSE.equals(prestamo.getActivo()))
                .toList();
        List<PrestamoMaquina> prestamosMaquinaria = prestamoMaquinaRepository.findAll().stream()
                .filter(prestamo -> !Boolean.FALSE.equals(prestamo.getActivo()))
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
        BigDecimal totalPrestado = BigDecimal.ZERO;
        BigDecimal totalCapitalPendiente = BigDecimal.ZERO;
        BigDecimal totalInteresPendiente = BigDecimal.ZERO;

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

        for (Prestamo prestamo : prestamosVehiculares) {
            BigDecimal amount = ceroSiNulo(prestamo.getMontoPrestamo());
            BigDecimal capitalPending = prestamo.getCapitalPendiente() != null
                    ? prestamo.getCapitalPendiente()
                    : amount.subtract(ceroSiNulo(prestamo.getCapitalPagado())).max(BigDecimal.ZERO);
            BigDecimal interestPending = ceroSiNulo(prestamo.getInteresPendiente());
            String guarantee = prestamo.getVehiculo() == null ? "Sin vehículo"
                    : valor(prestamo.getVehiculo().getMarca()) + " "
                    + valor(prestamo.getVehiculo().getLinea()) + " "
                    + valor(prestamo.getVehiculo().getPlaca());
            agregarFilaResumen(table, prestamo.getIdPrestamo(), "Vehicular",
                    nombreCliente(prestamo.getCliente()), guarantee, amount, capitalPending,
                    interestPending, prestamo.getEstadoPrestamo(), prestamo.getFechaVencimiento(), cellFont);
            count++;
            totalPrestado = totalPrestado.add(amount);
            totalCapitalPendiente = totalCapitalPendiente.add(capitalPending);
            totalInteresPendiente = totalInteresPendiente.add(interestPending);
        }

        for (PrestamoMaquina prestamo : prestamosMaquinaria) {
            BigDecimal amount = ceroSiNulo(prestamo.getMontoPrestamo());
            BigDecimal capitalPending = prestamo.getCapitalPendiente() != null
                    ? prestamo.getCapitalPendiente()
                    : amount.subtract(ceroSiNulo(prestamo.getCapitalPagado())).max(BigDecimal.ZERO);
            BigDecimal interestPending = ceroSiNulo(prestamo.getInteresPendiente());
            String guarantee = valor(prestamo.getTipoMaquina()) + " " + valor(prestamo.getMarcaMaquina());
            agregarFilaResumen(table, prestamo.getIdPrestamoMaquina(), "Maquinaria",
                    nombreCliente(prestamo.getCliente()), guarantee, amount, capitalPending,
                    interestPending, prestamo.getEstadoPrestamo(), prestamo.getFechaVencimiento(), cellFont);
            count++;
            totalPrestado = totalPrestado.add(amount);
            totalCapitalPendiente = totalCapitalPendiente.add(capitalPending);
            totalInteresPendiente = totalInteresPendiente.add(interestPending);
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
                        count, formatearMonto(totalPrestado), formatearMonto(totalCapitalPendiente),
                        formatearMonto(totalInteresPendiente)),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new BaseColor(31, 78, 121)));
        totals.setSpacingBefore(12);
        document.add(totals);
        document.close();
        return output.toByteArray();
    }

    private void agregarFilaResumen(
            PdfPTable table,
            Long idPrestamo,
            String tipo,
            String cliente,
            String garantia,
            BigDecimal monto,
            BigDecimal capitalPendiente,
            BigDecimal interesPendiente,
            EstadoPrestamo estado,
            Object vencimiento,
            Font font
    ) {
        List<String> values = List.of(
                valor(idPrestamo),
                tipo,
                cliente,
                garantia,
                formatearMonto(monto),
                formatearMonto(capitalPendiente),
                formatearMonto(interesPendiente),
                valor(estado) + " / " + valor(vencimiento)
        );
        for (String value : values) {
            PdfPCell cell = new PdfPCell(new Phrase(value, font));
            cell.setPadding(4);
            table.addCell(cell);
        }
    }

    private String nombreCliente(Cliente cliente) {
        return cliente == null ? "No registrado"
                : valor(cliente.getNombres()) + " " + valor(cliente.getApellidos());
    }

    private BigDecimal ceroSiNulo(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    @Transactional(readOnly = true)
    public byte[] generarReciboPago(Long idPago) throws Exception {
        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado"));
        Prestamo prestamo = pago.getPrestamo();
        Cliente cliente = prestamo != null ? prestamo.getCliente() : null;
        String garantia = prestamo != null && prestamo.getVehiculo() != null
                ? String.format("%s %s %s (placa %s)",
                valor(prestamo.getVehiculo().getMarca()),
                valor(prestamo.getVehiculo().getLinea()),
                valor(prestamo.getVehiculo().getModelo()),
                valor(prestamo.getVehiculo().getPlaca()))
                : "No registrada";

        return crearPdfRecibo(
                pago.getIdPago(),
                pago.getFechaPago(),
                pago.getMontoPagado(),
                pago.getTipoPago() != null ? pago.getTipoPago().name() : null,
                pago.getMetodoPago() != null ? pago.getMetodoPago().name() : null,
                pago.getObservaciones(),
                prestamo != null ? prestamo.getIdPrestamo() : null,
                prestamo != null && prestamo.getEstadoPrestamo() != null
                        ? prestamo.getEstadoPrestamo().name() : null,
                cliente,
                "Vehículo: " + garantia
        );
    }

    @Transactional(readOnly = true)
    public byte[] generarReciboPagoMaquina(Long idPagoMaquina) throws Exception {
        PagoMaquina pago = pagoMaquinaRepository.findById(idPagoMaquina)
                .orElseThrow(() -> new EntityNotFoundException("Pago de maquinaria no encontrado"));
        PrestamoMaquina prestamo = pago.getPrestamoMaquina();
        Cliente cliente = prestamo != null ? prestamo.getCliente() : null;
        String garantia = prestamo != null
                ? String.format("%s %s",
                valor(prestamo.getTipoMaquina()), valor(prestamo.getMarcaMaquina()))
                : "No registrada";

        return crearPdfRecibo(
                pago.getIdPagoMaquina(),
                pago.getFechaPago(),
                pago.getMontoPagado(),
                pago.getTipoPago() != null ? pago.getTipoPago().name() : null,
                pago.getMetodoPago() != null ? pago.getMetodoPago().name() : null,
                pago.getObservaciones(),
                prestamo != null ? prestamo.getIdPrestamoMaquina() : null,
                prestamo != null && prestamo.getEstadoPrestamo() != null
                        ? prestamo.getEstadoPrestamo().name() : null,
                cliente,
                "Maquinaria: " + garantia
        );
    }

    private byte[] crearPdfRecibo(
            Long idPago,
            java.time.LocalDateTime fechaPago,
            BigDecimal montoPagado,
            String tipoPago,
            String metodoPago,
            String observaciones,
            Long idPrestamo,
            String estadoPrestamo,
            Cliente cliente,
            String garantia
    ) throws DocumentException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A5, 42, 42, 36, 36);
        PdfWriter.getInstance(document, output);
        document.open();

        Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new BaseColor(31, 78, 121));
        Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.DARK_GRAY);
        Font etiqueta = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.DARK_GRAY);
        Font valor = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
        Font monto = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new BaseColor(31, 78, 121));

        Paragraph encabezado = new Paragraph("RECIBO DE PAGO", titulo);
        encabezado.setAlignment(Element.ALIGN_CENTER);
        document.add(encabezado);

        Paragraph referencia = new Paragraph("Comprobante No. " + idPago, subtitulo);
        referencia.setAlignment(Element.ALIGN_CENTER);
        referencia.setSpacingAfter(16);
        document.add(referencia);

        PdfPTable tabla = new PdfPTable(new float[]{1.1f, 2f});
        tabla.setWidthPercentage(100);
        tabla.setSpacingAfter(14);
        agregarFila(tabla, "Fecha y hora", fechaPago == null
                ? "No registrada"
                : fechaPago.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), etiqueta, valor);
        agregarFila(tabla, "Cliente", cliente == null ? "No registrado"
                : valor(cliente.getNombres()) + " " + valor(cliente.getApellidos()), etiqueta, valor);
        agregarFila(tabla, "Documento", cliente == null ? "No registrado"
                : valor(cliente.getNumeroDocumento()), etiqueta, valor);
        agregarFila(tabla, "Préstamo", idPrestamo == null ? "No registrado" : idPrestamo.toString(), etiqueta, valor);
        agregarFila(tabla, "Estado del préstamo", valor(estadoPrestamo), etiqueta, valor);
        agregarFila(tabla, "Concepto", valor(tipoPago), etiqueta, valor);
        agregarFila(tabla, "Método de pago", valor(metodoPago), etiqueta, valor);
        agregarFila(tabla, "Garantía", garantia, etiqueta, valor);
        agregarFila(tabla, "Observaciones", valor(observaciones), etiqueta, valor);
        document.add(tabla);

        Paragraph montoRecibido = new Paragraph("Monto recibido: " + formatearMonto(montoPagado), monto);
        montoRecibido.setAlignment(Element.ALIGN_RIGHT);
        montoRecibido.setSpacingBefore(8);
        document.add(montoRecibido);

        Paragraph pie = new Paragraph("Este documento certifica el registro del pago indicado.", subtitulo);
        pie.setAlignment(Element.ALIGN_CENTER);
        pie.setSpacingBefore(22);
        document.add(pie);

        document.close();
        return output.toByteArray();
    }

    private void agregarFila(PdfPTable tabla, String etiqueta, String valor, Font fuenteEtiqueta, Font fuenteValor) {
        PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, fuenteEtiqueta));
        celdaEtiqueta.setBorder(PdfPCell.NO_BORDER);
        celdaEtiqueta.setPadding(5);
        tabla.addCell(celdaEtiqueta);

        PdfPCell celdaValor = new PdfPCell(new Phrase(valor, fuenteValor));
        celdaValor.setBorder(PdfPCell.NO_BORDER);
        celdaValor.setPadding(5);
        tabla.addCell(celdaValor);
    }

    private String valor(Object texto) {
        return texto == null || texto.toString().isBlank() ? "No registrado" : texto.toString();
    }

    private String formatearMonto(BigDecimal monto) {
        if (monto == null) {
            return "No registrado";
        }
        NumberFormat formato = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));
        formato.setCurrency(Currency.getInstance("COP"));
        return formato.format(monto);
    }
}