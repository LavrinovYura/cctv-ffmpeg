package polytech.diploma.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import polytech.diploma.exceptions.UserNotFoundException;
import polytech.diploma.models.user.Person;
import polytech.diploma.models.user.Role;
import polytech.diploma.repositories.PersonRepository;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonDetailServiceImpl implements UserDetailsService {

    private final PersonRepository personRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       Person person = personRepository.findByUsername(username).
               orElseThrow(()-> new UserNotFoundException("User not found"));
        return new User(person.getUsername(), person.getPassword(), mapRolesToAuthorities(person.getRoles()));
    }

    private Collection<GrantedAuthority> mapRolesToAuthorities(Set<Role> roles){
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.getRoleType().toString())).collect(Collectors.toList());
    }
}