package control.prendario.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "Token de acceso y atribuciones del usuario")
public class TokenResponse {

    private String tokenType;
    private String accessToken;
    private long expiresIn;
    private List<String> roles;
}
