package polytech.diploma.dtos.administration;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RequestRole {
    @NotBlank(message = "Please enter role type")
    String roleType;
}
