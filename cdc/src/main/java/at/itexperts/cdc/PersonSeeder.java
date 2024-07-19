package at.itexperts.cdc;

import java.util.stream.IntStream;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;

@Component
public class PersonSeeder {

    private static final int NUMBER_OF_PERSONS = 1_000;

    private final PersonRepository personRepository;
    private final Faker faker = new Faker();

    public PersonSeeder(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @PostConstruct
    public void init() {
        IntStream.range(0, NUMBER_OF_PERSONS).forEach(i -> personRepository.save(createRandomPerson()));
    }

    private Person createRandomPerson() {
        Person person = new Person();
        person.setFirstName(faker.name().firstName());
        person.setLastName(faker.name().lastName());
        return person;
    }
}
