package polytech.diploma.dtos.authorization;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String accessToken;
    private String refreshToken;
    private final String tokenType = "Bearer ";
    private boolean admin;
    private String firstName;
    private String secondName;
    private String middleName;
}
