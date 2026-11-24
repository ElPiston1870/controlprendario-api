package control.prendario.security.config;

import control.prendario.security.controller.AuthController;
import control.prendario.security.service.JwtService;
import control.prendario.client.domain.Client;
import control.prendario.client.controller.ClientController;
import control.prendario.client.service.ClientService;
import control.prendario.client.domain.DuplicateDocumentException;
import control.prendario.loan.service.LoanService;
import control.prendario.loan.controller.PublicLoanController;
import control.prendario.movement.controller.MovementController;
import control.prendario.movement.service.MovementService;
import control.prendario.payment.service.MachineryPaymentService;
import control.prendario.payment.controller.PaymentController;
import control.prendario.payment.service.PaymentService;
import control.prendario.shared.config.GlobalExceptionHandler;
import control.prendario.shared.config.WebConfig;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import control.prendario.shared.query.BrowseQuery;
import control.prendario.shared.query.PageResult;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.argThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Reglas de acceso (RF-21, RN-16), CORS y validación de clientes (RN-01, RN-02).
 */
@WebMvcTest(
        controllers = {AuthController.class, ClientController.class, MovementController.class,
                PaymentController.class, PublicLoanController.class},
        properties = {
                "jwt.secret=unit-test-secret-with-at-least-32-bytes",
                "jwt.expiration=60000",
                "app.security.admin.username=test-gerente",
                "app.security.admin.password=test-gerente-password",
                "app.security.operator.username=test-empleado",
                "app.security.operator.password=test-empleado-password"
        }
)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtService.class, WebConfig.class,
        GlobalExceptionHandler.class})
@ActiveProfiles("local")
class SecurityRulesTest {

    private static final String ORIGIN = "http://localhost:4200";
    private static final String VALID_CLIENT = """
            {"tipoDocumento":"CEDULA","numeroDocumento":"1075000000","nombres":"Cliente",
             "apellidos":"Ficticio","ciudad":"Santa María","telefono":"3000000000"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;
    @MockBean
    private MovementService movementService;
    @MockBean
    private PaymentService paymentService;
    @MockBean
    private MachineryPaymentService machineryPaymentService;
    @MockBean
    private LoanService loanService;

    @Test
    void frontendCorsPreflightIsAcceptedWithoutToken() throws Exception {
        mockMvc.perform(options("/api/clientes")
                        .header("Origin", ORIGIN)
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN));
    }

    @Test
    void localProfileRejectsUnconfiguredCrossOriginRequests() throws Exception {
        mockMvc.perform(options("/api/clientes")
                        .header("Origin", "https://unconfigured.example")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void employeeManagesClients() throws Exception {
        String token = token("test-empleado", "test-empleado-password");
        when(clientService.pageClients(any(BrowseQuery.class))).thenReturn(new PageResult<>(List.of(), 1, 5, 0, 0));
        when(clientService.saveClient(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(get("/api/clientes/buscar").header("Authorization", "Bearer " + token)
                        .param("pagina", "1").param("tamano", "5")
                        .param("nombre", "Ana Pérez").param("numeroDocumento", "123"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.content").isArray());
        verify(clientService).pageClients(argThat(query -> query.pagina() == 1 && query.tamano() == 5
                && "Ana Pérez".equals(query.nombre()) && "123".equals(query.numeroDocumento())));
        mockMvc.perform(post("/api/clientes").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_CLIENT))
                .andExpect(status().isOk());
    }

    @Test
    void onlyManagerRegistersMovements() throws Exception {
        String movement = "{\"tipoMovimiento\":\"ENTRADA\",\"monto\":1000}";
        mockMvc.perform(post("/api/movimientos")
                        .header("Authorization", "Bearer " + token("test-empleado", "test-empleado-password"))
                        .contentType(MediaType.APPLICATION_JSON).content(movement))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/movimientos")
                        .header("Authorization", "Bearer " + token("test-gerente", "test-gerente-password"))
                        .contentType(MediaType.APPLICATION_JSON).content(movement))
                .andExpect(status().isOk());
    }

    @Test
    void employeeCanReadBalance() throws Exception {
        mockMvc.perform(get("/api/movimientos/balance")
                        .header("Authorization", "Bearer " + token("test-empleado", "test-empleado-password")))
                .andExpect(status().isOk());
    }

    @Test
    void loanQueryIsNoLongerPublic() throws Exception {
        mockMvc.perform(get("/api/public/prestamos/buscar"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clientWithoutRequiredDataIsRejectedWithDetails() throws Exception {
        mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + token("test-empleado", "test-empleado-password"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.numeroDocumento").exists())
                .andExpect(jsonPath("$.campos.nombres").exists())
                .andExpect(jsonPath("$.campos.telefono").exists());
    }

    @Test
    void duplicateDocumentReturnsConflict() throws Exception {
        when(clientService.saveClient(any(Client.class)))
                .thenThrow(new DuplicateDocumentException("Ya existe un cliente con ese número de documento"));
        mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + token("test-empleado", "test-empleado-password"))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_CLIENT))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Ya existe un cliente con ese número de documento"));
    }

    private String token(String username, String password) throws Exception {
        String responseBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonMapper.builder().build().readTree(responseBody).get("accessToken").asText();
    }
}
