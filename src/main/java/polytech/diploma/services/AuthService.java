package polytech.diploma.services;

import io.jsonwebtoken.Claims;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import polytech.diploma.dtos.authorization.JwtResponse;
import polytech.diploma.dtos.authorization.LoginDTO;
import polytech.diploma.dtos.authorization.LoginResponseDTO;
import polytech.diploma.exceptions.UserNotFoundException;
import polytech.diploma.exceptions.ValidationException;
import polytech.diploma.models.user.Person;
import polytech.diploma.repositories.PersonRepository;
import polytech.diploma.security.JWT.JWTProvider;

import java.util.HashMap;
import java.util.Map;

import static polytech.diploma.models.user.RoleType.ADMIN;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JWTProvider jwtProvider;
    private final PersonRepository personRepository;
    private final Map<String, String> refreshStorage = new HashMap<>();

    public LoginResponseDTO login(LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Person person = findPersonByUsername(loginDTO.getUsername());

        String accessToken = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(authentication);

        refreshStorage.put(authentication.getName(), refreshToken);

        LoginResponseDTO response = new LoginResponseDTO();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setFirstName(person.getFirstName());
        response.setSecondName(person.getSecondName());
        response.setMiddleName(person.getMiddleName());
        response.setAdmin(person.getRoles().stream().anyMatch(role -> role.getRoleType().equals(ADMIN)));

        return response;
    }

    public JwtResponse getAccessToken(String refreshToken) {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            String login = claims.getSubject();
            String saveRefreshToken = refreshStorage.get(login);
            if (saveRefreshToken != null && saveRefreshToken.equals(refreshToken)) {
                findPersonByUsername(login);
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String accessToken = jwtProvider.generateAccessToken(authentication);
                return new JwtResponse(accessToken, null);
            }
        }
        return new JwtResponse(null, null);
    }

    public JwtResponse refreshTokens(@NonNull String refreshToken) {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            String login = claims.getSubject();
            String saveRefreshToken = refreshStorage.get(login);
            if (saveRefreshToken != null && saveRefreshToken.equals(refreshToken)) {
                Person person = findPersonByUsername(login);
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String accessToken = jwtProvider.generateAccessToken(authentication);
                String newRefreshToken = jwtProvider.generateRefreshToken(authentication);
                refreshStorage.put(person.getUsername(), newRefreshToken);
                return new JwtResponse(accessToken, newRefreshToken);
            }
        }
        throw new ValidationException("Token", "Невалидный JWT токен");
    }

    public Person findPersonByUsername(String username) {
        return personRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("There is no person with name" + username));
    }
}
