package polytech.diploma.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import polytech.diploma.PersonMapper;
import polytech.diploma.dtos.authorization.RegisterDTO;
import polytech.diploma.dtos.authorization.RegisterResponseDTO;
import polytech.diploma.models.user.Person;
import polytech.diploma.models.user.Role;
import polytech.diploma.models.user.RoleType;
import polytech.diploma.repositories.PersonRepository;
import polytech.diploma.repositories.RoleRepository;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;

@Service
@Transactional
@RequiredArgsConstructor
public class RegistrationService {

    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final PersonMapper personMapper;

    @Value("${RegistrationService.user.expiration}")
    private long EXPIRE_DATE;

    public RegisterResponseDTO register(RegisterDTO registerDTO){
        Person person = new Person();

        person.setUsername(registerDTO.getUsername());
        person.setPassword(passwordEncoder.encode(registerDTO.getPassword()));

        person.setFirstName(StringUtils.capitalize(registerDTO.getFirstName().trim()));
        person.setSecondName(StringUtils.capitalize(registerDTO.getSecondName().trim()));
        person.setMiddleName(StringUtils.capitalize(registerDTO.getMiddleName().trim()));

        LocalDateTime now = LocalDateTime.now();
        Instant userExpireInstant = now.plusDays(EXPIRE_DATE).atZone(ZoneId.systemDefault()).toInstant();
        person.setExpireDate(Date.from(userExpireInstant));

        Role role = roleRepository.findByRoleType(RoleType.USER);
        person.setRoles(Collections.singleton(role));

        return personMapper.personToRegisterResponseDTO(personRepository.save(person));
    }

}
