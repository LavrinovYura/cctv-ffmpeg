package polytech.diploma.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import polytech.diploma.dtos.authorization.*;
import polytech.diploma.services.AuthService;
import polytech.diploma.services.RegistrationService;

import javax.validation.Valid;

@CrossOrigin(allowCredentials = "true", originPatterns = "*")
@RestController
@RequestMapping("api/auth/")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final RegistrationService registrationService;

    @PostMapping("login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginDTO loginDTO) {
        LoginResponseDTO response = authService.login(loginDTO);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("refresh")
    public ResponseEntity<JwtResponse> getNewRefreshToken(@RequestBody @Valid RefreshJwtRequest request) {
        JwtResponse response = authService.refreshTokens(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("token")
    public ResponseEntity<JwtResponse> getAccessToken(RefreshJwtRequest refreshToken) {
        JwtResponse response = authService.getAccessToken(refreshToken.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("register")
    public ResponseEntity<RegisterResponseDTO> register(@RequestBody @Valid RegisterDTO registerDTO) {
        RegisterResponseDTO response = registrationService.register(registerDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

}
