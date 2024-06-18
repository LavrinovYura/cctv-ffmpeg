package polytech.diploma.services;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import polytech.diploma.mappers.PersonMapper;
import polytech.diploma.dtos.administration.PersonDTO;
import polytech.diploma.exceptions.ConflictException;
import polytech.diploma.models.user.Person;
import polytech.diploma.models.user.Role;
import polytech.diploma.models.user.RoleType;
import polytech.diploma.repositories.PersonRepository;
import polytech.diploma.repositories.RoleRepository;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

    private final PersonRepository personRepository;
    private final RoleRepository roleRepository;
    private final PersonMapper personMapper;


    public Set<PersonDTO> getUsers(Pageable pageable) {
        Page<Person> page = personRepository.findAll(pageable);

        if (page.isEmpty()) {
            throw new EntityNotFoundException("No users in database");
        }

        return personMapper.personToDTOSet(page);
    }

    public Set<PersonDTO> getUsersByRole(Pageable pageable, String type) {
        Role role = getRoleByType(type);

        Page<Person> page = personRepository.findAllByRoles(role, pageable);

        if (page.isEmpty()) {
            throw new EntityNotFoundException("There are no users with this role in the database");
        }

        return personMapper.personToDTOSet(page);
    }

    public void deleteUser(Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no person with id " + id));

        personRepository.delete(person);
    }

    public void addRole(String type, Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no person with id " + id));

        Role role = getRoleByType(type);

        if (person.getRoles().contains(role)) {
            throw new ConflictException("This person already has this role.");
        }

        person.getRoles().add(role);
        personRepository.save(person);
    }

    public void removeRole(String type, Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no person with id " + id ));
        Role role = getRoleByType(type);

        if (person.getRoles().stream().noneMatch(role::equals)) {
            throw new ConflictException("This person doesn't have this role");
        }

        person.getRoles().remove(role);

        personRepository.save(person);
    }

    private Role getRoleByType(String type) {
        RoleType roleType;

        try {
            roleType = RoleType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new EntityNotFoundException("There is no such role exist");
        }

        return roleRepository.findByRoleType(roleType);
    }

}
