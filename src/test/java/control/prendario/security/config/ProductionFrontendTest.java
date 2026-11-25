package control.prendario.security.config;

import control.prendario.security.controller.AuthController;
import control.prendario.security.service.JwtService;
import control.prendario.shared.config.FrontendWebConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class, properties = "jwt.secret=test-key-for-docker-frontend-do-not-use")
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtService.class, FrontendWebConfig.class})
@ActiveProfiles("prod")
class ProductionFrontendTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void opensAngularEntryAndDeepLinksWithoutAnApiToken() throws Exception {
        for (String route : new String[]{"/", "/login", "/auditoria", "/clientes", "/prestamos/maquina/ver/42"}) {
            mvc.perform(get(route))
                    .andExpect(status().isOk())
                    .andExpect(forwardedUrl("/index.html"));
        }
    }

    @Test
    void spaFallbackDoesNotExposeApiRoutes() throws Exception {
        for (String route : new String[]{"/api/clientes", "/api/auditoria", "/api/clientes/view.js"}) {
            mvc.perform(get(route)).andExpect(status().isUnauthorized());
        }
    }
}
