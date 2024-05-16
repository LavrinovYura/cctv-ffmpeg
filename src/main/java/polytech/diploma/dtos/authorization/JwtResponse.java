package polytech.diploma.dtos.authorization;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtResponse {
    String accessToken;
    String refreshToken;
}
