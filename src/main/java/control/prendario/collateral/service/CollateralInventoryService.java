package control.prendario.collateral.service;

import control.prendario.collateral.dto.CollateralInventoryDTO;
import control.prendario.loan.domain.Loan;
import control.prendario.loan.repository.LoanRepository;
import control.prendario.loan.domain.LoanStatus;
import control.prendario.loan.domain.MachineryLoan;
import control.prendario.loan.repository.MachineryLoanRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import control.prendario.shared.query.BrowseQuery;
import control.prendario.shared.query.CatalogFilters;
import control.prendario.shared.query.PageResult;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CollateralInventoryService {

    private final LoanRepository loanRepository;
    private final MachineryLoanRepository machineryLoanRepository;

    private final NamedParameterJdbcTemplate jdbc;

    private static final String INVENTORY = """
            SELECT 'VEHICULO' AS tipo, p.id_prestamo AS id, p.fecha_prestamo AS fecha,
                   c.nombres, c.apellidos, c.numero_documento, v.placa AS identificador,
                   CONCAT_WS(' ', v.tipo_vehiculo, v.marca, v.linea, v.modelo) AS descripcion
            FROM prestamos p
            LEFT JOIN clientes c ON c.id_cliente = p.id_cliente
            LEFT JOIN vehiculos v ON v.id_vehiculo = p.id_vehiculo
            WHERE (p.activo = TRUE OR p.activo IS NULL)
            UNION ALL
            SELECT 'MAQUINARIA', p.id_prestamo_maquina, p.fecha_prestamo,
                   c.nombres, c.apellidos, c.numero_documento, p.tipo_maquina, p.marca
            FROM prestamos_maquinas p
            LEFT JOIN clientes c ON c.id_cliente = p.id_cliente
            WHERE (p.activo = TRUE OR p.activo IS NULL)
            """;

    private record InventoryKey(String type, Long id) {}

    @Transactional(readOnly = true)
    public PageResult<CollateralInventoryDTO> getInventory(BrowseQuery query) {
        var page = query.page(Sort.unsorted());
        var params = new MapSqlParameterSource("limit", page.getPageSize()).addValue("offset", page.getOffset());
        var from = new StringBuilder(" FROM (" + INVENTORY + ") inventory WHERE 1 = 1");
        if (CatalogFilters.hasText(query.tipoGarantia())) {
            params.addValue("type", query.tipoGarantia().strip().toUpperCase(Locale.ROOT));
            from.append(" AND tipo = :type");
        }
        int index = 0;
        for (String token : CatalogFilters.tokens(query.termino())) {
            String name = "term" + index++;
            params.addValue(name, CatalogFilters.pattern(token));
            from.append("""
                     AND (LOWER(nombres) LIKE :TERM ESCAPE '!' OR LOWER(apellidos) LIKE :TERM ESCAPE '!'
                     OR LOWER(numero_documento) LIKE :TERM ESCAPE '!' OR LOWER(tipo) LIKE :TERM ESCAPE '!'
                     OR LOWER(identificador) LIKE :TERM ESCAPE '!' OR LOWER(descripcion) LIKE :TERM ESCAPE '!'
                     OR CONCAT('', id) LIKE :TERM ESCAPE '!')
                     """.replace(":TERM", ":" + name));
        }

        long total = jdbc.queryForObject("SELECT COUNT(*)" + from, params, Long.class);
        var keys = page.getOffset() >= total ? List.<InventoryKey>of()
                : jdbc.query("SELECT tipo, id" + from + " ORDER BY fecha DESC, tipo, id DESC LIMIT :limit OFFSET :offset",
                        params, (row, number) -> new InventoryKey(row.getString("tipo"), row.getLong("id")));
        var byKey = new HashMap<InventoryKey, CollateralInventoryDTO>();
        var vehicleIds = keys.stream().filter(key -> key.type().equals("VEHICULO")).map(InventoryKey::id).toList();
        var machineIds = keys.stream().filter(key -> key.type().equals("MAQUINARIA")).map(InventoryKey::id).toList();
        if (!vehicleIds.isEmpty()) loanRepository.findByLoanIdIn(vehicleIds).forEach(
                loan -> byKey.put(new InventoryKey("VEHICULO", loan.getLoanId()), inventoryItem(loan)));
        if (!machineIds.isEmpty()) machineryLoanRepository.findByMachineryLoanIdIn(machineIds).forEach(
                loan -> byKey.put(new InventoryKey("MAQUINARIA", loan.getMachineryLoanId()), inventoryItem(loan)));
        var items = keys.stream().map(byKey::get).filter(Objects::nonNull).toList();
        return PageResult.from(new PageImpl<>(items, page, total));
    }

    private CollateralInventoryDTO inventoryItem(Loan loan) {
        String collateral = loan.getVehicle() == null
                ? "Sin vehículo asociado"
                : String.format("%s %s %s %s",
                value(loan.getVehicle().getVehicleType()),
                value(loan.getVehicle().getBrand()),
                value(loan.getVehicle().getLine()),
                value(loan.getVehicle().getModel()));
        String plate = loan.getVehicle() == null
                ? "No registrada" : value(loan.getVehicle().getPlate());
        return CollateralInventoryDTO.builder()
                .collateralType("VEHICULO")
                .loanId(loan.getLoanId())
                .clientId(loan.getClient() == null ? null : loan.getClient().getClientId())
                .clientName(clientName(loan))
                .identifier(plate)
                .description(collateral)
                .loanAmount(loan.getLoanAmount())
                .pendingPrincipal(amountOrFallback(loan.getPendingPrincipal(), loan.getLoanAmount()))
                .loanStatus(value(loan.getLoanStatus()))
                .loanDate(loan.getLoanDate())
                .dueDate(value(loan.getDueDate()))
                .inCollateral(!isLoanSettled(loan.getLoanStatus()))
                .build();
    }

    private CollateralInventoryDTO inventoryItem(MachineryLoan loan) {
        return CollateralInventoryDTO.builder()
                .collateralType("MAQUINARIA")
                .loanId(loan.getMachineryLoanId())
                .clientId(loan.getClient() == null ? null : loan.getClient().getClientId())
                .clientName(clientName(loan))
                .identifier(value(loan.getMachineType()) + " " + value(loan.getMachineBrand()))
                .description("Maquinaria en garantía")
                .loanAmount(loan.getLoanAmount())
                .pendingPrincipal(amountOrFallback(loan.getPendingPrincipal(), loan.getLoanAmount()))
                .loanStatus(value(loan.getLoanStatus()))
                .loanDate(loan.getLoanDate())
                .dueDate(value(loan.getDueDate()))
                .inCollateral(!isLoanSettled(loan.getLoanStatus()))
                .build();
    }

    private String clientName(Loan loan) {
        if (loan.getClient() == null) {
            return "Cliente no registrado";
        }
        return value(loan.getClient().getFirstNames()) + " " + value(loan.getClient().getLastNames());
    }

    private String clientName(MachineryLoan loan) {
        if (loan.getClient() == null) {
            return "Cliente no registrado";
        }
        return value(loan.getClient().getFirstNames()) + " " + value(loan.getClient().getLastNames());
    }

    private boolean isLoanSettled(LoanStatus status) {
        return status == LoanStatus.PAGADO;
    }

    private BigDecimal amountOrFallback(BigDecimal amount, BigDecimal fallback) {
        return amount != null ? amount : (fallback != null ? fallback : BigDecimal.ZERO);
    }

    private String value(Object value) {
        return value == null || value.toString().isBlank() ? "No registrado" : value.toString();
    }
}
