package control.prendario.security.config;

import control.prendario.security.controller.AuthController;
import control.prendario.security.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        properties = {
                "jwt.secret=unit-test-secret-with-at-least-32-bytes",
                "jwt.expiration=60000",
                "app.security.admin.username=test-admin",
                "app.security.admin.password=test-admin-password",
                "app.security.operator.username=test-operator",
                "app.security.operator.password=test-operator-password"
        }
)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtService.class})
class AuthSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginReturnsJwtWithUserRole() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"test-operator","password":"test-operator-password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_OPERATOR"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void operatorCannotReadAudit() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"test-operator","password":"test-operator-password"}
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String token = com.fasterxml.jackson.databind.json.JsonMapper.builder()
                .build()
                .readTree(response)
                .get("accessToken")
                .asText();

        mockMvc.perform(get("/api/auditoria")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void managementEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isUnauthorized());
    }
}
