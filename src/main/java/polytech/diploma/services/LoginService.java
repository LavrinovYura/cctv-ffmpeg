package polytech.diploma.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import polytech.diploma.exceptions.ResourceNotFoundException;
import polytech.diploma.models.user.Person;
import polytech.diploma.repositories.PersonRepository;

import java.util.Optional;

@Service
public class LoginService {

    private final PersonRepository personRepository;

    @Autowired
    public LoginService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public Person findPersonByUsername(String username){
        Optional<Person> optionalPerson = personRepository.findByUsername(username);
        if(!optionalPerson.isPresent())
            throw new ResourceNotFoundException("There is no person with name" + username);
        return optionalPerson.get();
    }
}
