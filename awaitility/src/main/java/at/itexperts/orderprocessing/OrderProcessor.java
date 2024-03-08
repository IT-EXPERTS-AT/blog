package at.itexperts.orderprocessing;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class OrderProcessor {

  private static final String INCOMING_ORDER_QUEUE = "incomingOrder";
  private static final String ORDER_EXCHANGE = "orderExchange";

  private final Logger logger = LoggerFactory.getLogger(OrderProcessor.class);
  private final AmqpTemplate amqpTemplate;

  public OrderProcessor(AmqpTemplate amqpTemplate) {
    this.amqpTemplate = amqpTemplate;
  }

  @RabbitListener(queues = INCOMING_ORDER_QUEUE)
  public void processMessage(IncomingOrder incomingOrder) {
    logger.info("Received incoming order: {}", incomingOrder);
  }

  @PostConstruct
  public void sendToIncomingAfterConstruct() {
    amqpTemplate.convertAndSend(ORDER_EXCHANGE, INCOMING_ORDER_QUEUE, new IncomingOrder(1, 2));
  }
}
