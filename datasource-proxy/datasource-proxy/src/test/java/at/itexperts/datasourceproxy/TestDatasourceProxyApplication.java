package at.itexperts.datasourceproxy;

import org.springframework.boot.SpringApplication;

public class TestDatasourceProxyApplication {

   public static void main(String[] args) {
      SpringApplication.from(DatasourceProxyApplication::main).with(TestcontainersConfiguration.class).run(args);
   }

}
