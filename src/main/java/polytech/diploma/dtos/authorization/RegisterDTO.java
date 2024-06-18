package polytech.diploma.dtos.authorization;

import lombok.Data;
import polytech.diploma.models.user.RoleType;

import javax.validation.constraints.*;
import java.util.List;

@Data
public class RegisterDTO {
    @NotBlank(message = "Please enter your name")
    @NotNull
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    @Pattern(regexp = "^[A-Za-zА-Юа-ю]+$", message = "Name must consist of only letters")
    private String firstName;

    @NotBlank(message = "Please enter your last name")
    @NotNull
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    @Pattern(regexp = "^[A-Za-zА-Юа-ю]+$", message = "Last name must consist of only letters")
    private String secondName;

    @NotBlank(message = "Please enter your middle name")
    @NotNull
    @Size(min = 5, max = 100, message = "Middle name must be between 5 and 100 characters")
    @Pattern(regexp = "^[A-Za-zА-Юа-ю]+$", message = "Middle name must consist of only letters")
    private String middleName;

    @NotEmpty(message = "Please enter role of user")
    private List<RoleType> roles;

    @NotNull
    private String username;

    @NotNull
    private String password;
}
