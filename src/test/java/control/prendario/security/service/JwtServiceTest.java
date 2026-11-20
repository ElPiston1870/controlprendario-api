package control.prendario.security.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtServiceTest {

    @Test
    void createsSignedTokenWithUserAndConfiguredExpiration() {
        JwtService jwtService = new JwtService("local-test-secret-with-at-least-32-bytes", 60_000);
        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "operador",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_OPERATOR"))
                );

        String token = jwtService.generateToken(authentication);

        assertEquals("operador", jwtService.extractUsername(token));
        assertEquals(60, jwtService.getExpirationSeconds());
    }
}
