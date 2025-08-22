package at.itexperts.datasourceproxy;

import static org.assertj.core.api.Assertions.assertThat;

import net.ttddyy.dsproxy.QueryCountHolder;
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
class PersonSeederTest {

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @Autowired private PersonSeeder personSeeder;
  @Autowired private QueryTracker queryTracker;

  @BeforeEach
  void setUp() {
    QueryCountHolder.clear();
  }

  @Test
  void seed() {
    personSeeder.seed();

    assertThat(QueryCountHolder.getGrandTotal().getInsert()).isEqualTo(1000);
  }

  @Test
  void trackInserts() {
    queryTracker.track(QueryType.INSERT);

    personSeeder.seed();

    queryTracker.assertInsertIntoCount("PERSON").isEqualTo(1000);
  }
}
