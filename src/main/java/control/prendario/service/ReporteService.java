package control.prendario.service;

import control.prendario.model.Cliente;
import control.prendario.model.Pago;
import control.prendario.model.PagoMaquina;
import control.prendario.model.Prestamo;
import control.prendario.model.PrestamoMaquina;
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