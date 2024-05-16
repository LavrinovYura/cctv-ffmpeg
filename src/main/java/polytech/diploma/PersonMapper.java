package polytech.diploma;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import polytech.diploma.dtos.administration.PersonDTO;
import polytech.diploma.dtos.authorization.RegisterResponseDTO;
import polytech.diploma.models.user.Person;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface PersonMapper {
    RegisterResponseDTO personToRegisterResponseDTO(Person person);

    @Named(value = "personToDTO")
    PersonDTO personToDTO(Person person);

    @IterableMapping(qualifiedByName = "personToDTO")
    Set<PersonDTO> personToDTOSet(Page<Person> persons);
}
