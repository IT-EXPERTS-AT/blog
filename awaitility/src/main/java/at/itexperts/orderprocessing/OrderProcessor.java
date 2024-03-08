package at.itexperts.orderprocessing;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class OrderProcessor {

  private static final String INCOMING_ORDER_QUEUE = "incomingOrderQueue";
  private static final String INCOMING_ORDER_ROUTING_KEY = "incomingOrder";
  private static final String OUTGOING_ORDER_ROUTING_KEY = "outgoingOrder";
  private static final String ORDER_EXCHANGE = "orderExchange";

  private final Logger logger = LoggerFactory.getLogger(OrderProcessor.class);
  private final AmqpTemplate amqpTemplate;

  public OrderProcessor(AmqpTemplate amqpTemplate) {
    this.amqpTemplate = amqpTemplate;
  }

  private static ProductCategory determineProductCategory(long productId) {
    if (productId < 100) {
      return ProductCategory.ELECTRONICS;
    } else if (productId > 100 && productId < 200) {
      return ProductCategory.BOOKS;
    }
    return ProductCategory.FOOD;
  }

  @RabbitListener(queues = INCOMING_ORDER_QUEUE)
  public void processMessage(IncomingOrder incomingOrder) {
    logger.info("Received incoming order: {}", incomingOrder);
    OutgoingOrder outgoingOrder = transformIntoOutgoingMessage(incomingOrder);
    amqpTemplate.convertAndSend(ORDER_EXCHANGE, OUTGOING_ORDER_ROUTING_KEY, outgoingOrder);
    logger.info("Sent outgoing order: {}", outgoingOrder);
  }

  private OutgoingOrder transformIntoOutgoingMessage(IncomingOrder incomingOrder) {
    return new OutgoingOrder(incomingOrder.id(), incomingOrder.productId(), determineProductCategory(incomingOrder.productId()));
  }

  @PostConstruct
  public void sendToIncomingAfterConstruct() {
    logger.info("Sending incoming order");
    amqpTemplate.convertAndSend(
        ORDER_EXCHANGE, INCOMING_ORDER_ROUTING_KEY, new IncomingOrder(1, 2));
  }
}
