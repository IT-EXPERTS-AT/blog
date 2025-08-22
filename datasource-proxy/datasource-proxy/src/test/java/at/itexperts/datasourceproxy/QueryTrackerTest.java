package at.itexperts.datasourceproxy;

import net.ttddyy.dsproxy.QueryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@Import(QueryTrackerConfig.class)
class QueryTrackerTest {

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @Autowired private PersonSeeder personSeeder;
  @Autowired private QueryTracker queryTracker;
  @Autowired private PersonRepository personRepository;

  @BeforeEach
  void setUp() {
    queryTracker.clear();
  }

  @Test
  void trackInserts() {
    queryTracker.track(QueryType.INSERT);

    personSeeder.seed();

    queryTracker.assertInsertIntoCount("PERSON").isEqualTo(1000);
  }

  @Test
  void trackDeletes() {
    queryTracker.track(QueryType.INSERT, QueryType.DELETE);

    personSeeder.seed();

    personRepository.deleteAll();

    queryTracker.assertInsertIntoCount("PERSON").isEqualTo(1000);
    queryTracker.assertDeleteCount("PERSON").isEqualTo(1000);
  }
}
