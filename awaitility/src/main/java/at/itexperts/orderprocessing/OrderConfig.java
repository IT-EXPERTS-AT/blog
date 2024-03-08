package at.itexperts.orderprocessing;

import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderConfig {

  @Bean
  public MessageConverter messageConverter() {
    DefaultClassMapper defaultClassMapper = new DefaultClassMapper();
    defaultClassMapper.setTrustedPackages("at.itexperts.orderprocessing");
    Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
    jackson2JsonMessageConverter.setClassMapper(defaultClassMapper);
    return jackson2JsonMessageConverter;
  }
}
