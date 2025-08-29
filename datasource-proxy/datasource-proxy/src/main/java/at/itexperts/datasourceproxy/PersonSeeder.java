package at.itexperts.datasourceproxy;

import com.github.javafaker.Faker;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;

@Component
public class PersonSeeder {

  private static final int NUMBER_OF_PERSONS = 1_000;

  private final PersonRepository personRepository;

  private final Faker faker = new Faker();

  public PersonSeeder(PersonRepository personRepository) {
    this.personRepository = personRepository;
  }

  public void seed() {
    IntStream.range(0, NUMBER_OF_PERSONS).forEach(i -> personRepository.save(createRandomPerson()));
  }

  private Person createRandomPerson() {
    var person = new Person();
    person.setFirstName(faker.name().firstName());
    person.setLastName(faker.name().lastName());
    return person;
  }
}
