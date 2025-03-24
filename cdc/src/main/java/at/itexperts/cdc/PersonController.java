package at.itexperts.cdc;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.javafaker.Faker;

@RestController
@RequestMapping("/person")
public class PersonController {

    private final PersonRepository personRepository;
    private final Faker faker = Faker.instance();

    public PersonController(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @PostMapping
    public void createPerson(@RequestBody PersonCreateReq personCreateReq) {
        var person = new Person();
        person.setFirstName(personCreateReq.firstName());
        person.setLastName(personCreateReq.lastName());

        personRepository.save(person);
    }

    @PostMapping("/random")
    public void createRandomPerson() {
        var person = new Person();

        person.setFirstName(faker.name().firstName());
        person.setLastName(faker.name().lastName());

        personRepository.save(person);
    }

    @DeleteMapping("/{id}")
    public void deletePerson(@PathVariable Long id) {
        personRepository.deleteById(id);
    }

    record PersonCreateReq(String firstName, String lastName) {

    }
}
