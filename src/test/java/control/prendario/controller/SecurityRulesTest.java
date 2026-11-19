package control.prendario.controller;

import control.prendario.config.GlobalExceptionHandler;
import control.prendario.config.JwtAuthenticationFilter;
import control.prendario.config.SecurityConfig;
import control.prendario.config.WebConfig;
import control.prendario.model.Cliente;
import control.prendario.service.ClienteService;
import control.prendario.service.DocumentoDuplicadoException;
import control.prendario.service.JwtService;
import control.prendario.service.MovimientoService;
import control.prendario.service.PagoMaquinaService;
import control.prendario.service.PagoService;
import control.prendario.service.PrestamoService;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

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
        controllers = {AuthController.class, ClienteController.class, MovimientoController.class,
                PagoController.class, PublicPrestamoController.class},
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
class SecurityRulesTest {

    private static final String ORIGEN = "https://controlprendario.netlify.app";
    private static final String CLIENTE_VALIDO = """
            {"tipoDocumento":"CEDULA","numeroDocumento":"1075000000","nombres":"Cliente",
             "apellidos":"Ficticio","ciudad":"Santa María","telefono":"3000000000"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClienteService clienteService;
    @MockBean
    private MovimientoService movimientoService;
    @MockBean
    private PagoService pagoService;
    @MockBean
    private PagoMaquinaService pagoMaquinaService;
    @MockBean
    private PrestamoService prestamoService;

    @Test
    void preflightCorsDelFrontendSeAceptaSinToken() throws Exception {
        mockMvc.perform(options("/api/clientes")
                        .header("Origin", ORIGEN)
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGEN));
    }

    @Test
    void empleadoGestionaClientes() throws Exception {
        String token = token("test-empleado", "test-empleado-password");
        when(clienteService.obtenerTodos()).thenReturn(List.of());
        when(clienteService.guardarCliente(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(get("/api/clientes").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/clientes").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(CLIENTE_VALIDO))
                .andExpect(status().isOk());
    }

    @Test
    void soloElGerenteRegistraMovimientos() throws Exception {
        String movimiento = "{\"tipoMovimiento\":\"ENTRADA\",\"monto\":1000}";
        mockMvc.perform(post("/api/movimientos")
                        .header("Authorization", "Bearer " + token("test-empleado", "test-empleado-password"))
                        .contentType(MediaType.APPLICATION_JSON).content(movimiento))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/movimientos")
                        .header("Authorization", "Bearer " + token("test-gerente", "test-gerente-password"))
                        .contentType(MediaType.APPLICATION_JSON).content(movimiento))
                .andExpect(status().isOk());
    }

    @Test
    void empleadoConsultaElBalance() throws Exception {
        mockMvc.perform(get("/api/movimientos/balance")
                        .header("Authorization", "Bearer " + token("test-empleado", "test-empleado-password")))
                .andExpect(status().isOk());
    }

    @Test
    void consultaDePrestamosYaNoEsPublica() throws Exception {
        mockMvc.perform(get("/api/public/prestamos/buscar"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteSinDatosObligatoriosSeRechazaConDetalle() throws Exception {
        mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + token("test-empleado", "test-empleado-password"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.numeroDocumento").exists())
                .andExpect(jsonPath("$.campos.nombres").exists())
                .andExpect(jsonPath("$.campos.telefono").exists());
    }

    @Test
    void documentoDuplicadoDevuelveConflicto() throws Exception {
        when(clienteService.guardarCliente(any(Cliente.class)))
                .thenThrow(new DocumentoDuplicadoException("Ya existe un cliente con ese número de documento"));
        mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + token("test-empleado", "test-empleado-password"))
                        .contentType(MediaType.APPLICATION_JSON).content(CLIENTE_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Ya existe un cliente con ese número de documento"));
    }

    private String token(String usuario, String clave) throws Exception {
        String respuesta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + usuario + "\",\"password\":\"" + clave + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonMapper.builder().build().readTree(respuesta).get("accessToken").asText();
    }
}
