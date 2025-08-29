package at.itexperts.datasourceproxy;

import java.util.HashSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueryTrackerConfig {

  @Bean
  public QueryTracker queryTracker() {
    return new QueryTracker(new HashSet<>());
  }
}
