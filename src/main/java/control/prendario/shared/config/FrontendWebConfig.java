package control.prendario.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile("prod")
public class FrontendWebConfig implements WebMvcConfigurer {
    public static final String[] ROUTES = {
            "/", "/login", "/home", "/configuracion", "/clientes/**", "/prestamos/**",
            "/pagos/**", "/movimientos/**", "/garantias", "/auditoria"
    };
    public static final String[] ASSETS = {
            "/index.html", "/assets/**", "/media/**", "/*.js", "/*.css", "/favicon.ico"
    };

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        for (String path : ROUTES) {
            registry.addViewController(path).setViewName("forward:/index.html");
        }
    }
}
