package polytech.diploma.services;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import polytech.diploma.models.user.Person;
import polytech.diploma.repositories.PersonRepository;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonExpirationService {

    private final PersonRepository personRepository;

    @Scheduled(fixedRate = 120000) // каждые 2 минуты
        public void deleteExpiredUsers() {
            Date now = new Date();
            List<Person> expiredUsers = personRepository.findByExpireDateBefore(now);
            personRepository.deleteAll(expiredUsers);
        }
}
