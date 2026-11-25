package control.prendario.security.config;

import control.prendario.shared.config.FrontendWebConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            PasswordEncoder passwordEncoder,
            @Value("${app.security.admin.username:}") String adminUsername,
            @Value("${app.security.admin.password:}") String adminPassword,
            @Value("${app.security.operator.username:}") String operatorUsername,
            @Value("${app.security.operator.password:}") String operatorPassword
    ) {
        List<UserDetails> users = new ArrayList<>();
        addUser(users, adminUsername, adminPassword, "ADMIN", passwordEncoder);
        addUser(users, operatorUsername, operatorPassword, "OPERATOR", passwordEncoder);
        if (users.isEmpty()) {
            logger.warn("No hay usuarios configurados. Define ADMIN_USERNAME/ADMIN_PASSWORD y/o OPERATOR_USERNAME/OPERATOR_PASSWORD.");
        }
        return new InMemoryUserDetailsManager(users);
    }

    private void addUser(
            List<UserDetails> users,
            String username,
            String password,
            String role,
            PasswordEncoder passwordEncoder
    ) {
        boolean usernameConfigured = username != null && !username.isBlank();
        boolean passwordConfigured = password != null && !password.isBlank();
        if (usernameConfigured != passwordConfigured) {
            throw new IllegalStateException("Debes configurar usuario y contraseña juntos para el rol " + role);
        }
        if (!usernameConfigured) {
            return;
        }
        if (users.stream().anyMatch(user -> user.getUsername().equalsIgnoreCase(username))) {
            throw new IllegalStateException("Cada usuario configurado debe ser único");
        }
        users.add(User.withUsername(username)
                .password(passwordEncoder.encode(password))
                .roles(role)
                .build());
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            DaoAuthenticationProvider authenticationProvider
    ) {
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            DaoAuthenticationProvider authenticationProvider,
            Environment environment
    ) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                response.sendError(401, "Autenticación requerida")))
                .authorizeHttpRequests(authorize -> {
                    if (environment.matchesProfiles("prod")) {
                        authorize.requestMatchers(HttpMethod.GET, FrontendWebConfig.ROUTES).permitAll()
                                .requestMatchers(HttpMethod.HEAD, FrontendWebConfig.ROUTES).permitAll()
                                .requestMatchers(HttpMethod.GET, FrontendWebConfig.ASSETS).permitAll()
                                .requestMatchers(HttpMethod.HEAD, FrontendWebConfig.ASSETS).permitAll();
                    }
                    authorize
                        // Preflight CORS del navegador (no lleva token)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error", "/api/auth/login",
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // RN-16 / RF-21: solo el gerente registra movimientos independientes
                        .requestMatchers(HttpMethod.POST, "/api/movimientos", "/api/movimientos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/movimientos", "/api/movimientos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/movimientos", "/api/movimientos/**").hasRole("ADMIN")
                        // La auditoría es una consulta de control del gerente
                        .requestMatchers("/api/auditoria", "/api/auditoria/**").hasRole("ADMIN")
                        // Gerente y empleado gestionan clientes, garantías, préstamos, pagos,
                        // descuentos, correcciones, contratos y consultas (informe, sección 4.3)
                        .anyRequest().hasAnyRole("ADMIN", "OPERATOR");
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
