package polytech.diploma.dtos.authorization;

import lombok.Data;
import polytech.diploma.models.user.Role;

import java.util.Set;

@Data
public class RegisterResponseDTO {
    private String firstName;
    private String secondName;
    private String middleName;
    private Set<Role> roles;
}
