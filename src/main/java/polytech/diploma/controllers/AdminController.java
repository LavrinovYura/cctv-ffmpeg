package polytech.diploma.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import polytech.diploma.dtos.administration.PersonDTO;
import polytech.diploma.dtos.administration.RequestRole;
import polytech.diploma.dtos.authorization.RegisterDTO;
import polytech.diploma.dtos.authorization.RegisterResponseDTO;
import polytech.diploma.services.AdminService;
import polytech.diploma.services.RegistrationService;

import javax.validation.Valid;
import java.util.Set;

@CrossOrigin(allowCredentials = "true", originPatterns = "*")
@RestController
@RequestMapping("api/admin/")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final RegistrationService registrationService;

    @PostMapping("users")
    public ResponseEntity<Set<PersonDTO>> getUsers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Set<PersonDTO> persons = adminService.getUsers(pageable);

        return ResponseEntity.ok().body(persons);
    }

    @DeleteMapping("users/{personId}/delete")
    public ResponseEntity<Void> deleteUser(@PathVariable Long personId) {
        adminService.deleteUser(personId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("users/{personId}/addRole")
    public ResponseEntity<Void> addRole(
            @RequestBody @Valid RequestRole requestRole,
            @PathVariable Long personId
    ) {
        adminService.addRole(requestRole.getRoleType(), personId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("users/{personId}/removeRole")
    public ResponseEntity<Void> removeRole(
            @RequestBody @Valid RequestRole requestRole,
            @PathVariable Long personId) {
        adminService.removeRole(requestRole.getRoleType(), personId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("users/usersByRole")
    public ResponseEntity<Set<PersonDTO>> getUsersByRole(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size,
            @RequestBody @Valid RequestRole requestRole
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Set<PersonDTO> persons = adminService.getUsersByRole(pageable, requestRole.getRoleType());

        return ResponseEntity.ok(persons);
    }

    @PostMapping("users/register")
    public ResponseEntity<RegisterResponseDTO> register(@RequestBody @Valid RegisterDTO registerDTO) {
        RegisterResponseDTO response = registrationService.register(registerDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

}
