package polytech.diploma.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import polytech.diploma.dtos.authorization.*;
import polytech.diploma.security.JWT.JWTProvider;
import polytech.diploma.services.AuthService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@CrossOrigin(allowCredentials = "true", originPatterns = "*")
@RestController
@RequestMapping("api/auth/")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JWTProvider jwtProvider;

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

    @GetMapping("validate-token")
    public ResponseEntity<Void> validateToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        if (jwtProvider.validateAccessToken(token)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}
