package at.itexperts.orderprocessing;

import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class OrderProcessingService {

  public static final String INCOMING_ORDER_QUEUE = "incomingOrderQueue";
  public static final String INCOMING_ORDER_ROUTING_KEY = "incomingOrder";
  public static final String OUTGOING_ORDER_ROUTING_KEY = "outgoingOrder";
  public static final String OUTGOING_ORDER_QUEUE = "outgoingOrderQueue";
  public static final String ORDER_EXCHANGE = "orderExchange";

  private final Logger logger = LoggerFactory.getLogger(OrderProcessingService.class);
  private final AmqpTemplate amqpTemplate;
  private final Set<Long> receivedOrderIds;

  public OrderProcessingService(AmqpTemplate amqpTemplate) {
    this.amqpTemplate = amqpTemplate;
    this.receivedOrderIds = new HashSet<>();
  }

  public boolean wasOrderProcessed(long orderId) {
    logger.info("Checking if order with id:{} was processed", orderId);
    return receivedOrderIds.contains(orderId);
  }

  @RabbitListener(queues = INCOMING_ORDER_QUEUE)
  public void processOrder(IncomingOrder incomingOrder) {
    logger.info("Received incoming order: {}", incomingOrder);

    OutgoingOrder outgoingOrder = transformIntoOutgoingMessage(incomingOrder);
    sendOutgoingOrder(outgoingOrder);

    receivedOrderIds.add(incomingOrder.id());
  }

  private void sendOutgoingOrder(OutgoingOrder outgoingOrder) {
    amqpTemplate.convertAndSend(ORDER_EXCHANGE, OUTGOING_ORDER_ROUTING_KEY, outgoingOrder);
    logger.info("Sent outgoing order: {}", outgoingOrder);
  }

  private OutgoingOrder transformIntoOutgoingMessage(IncomingOrder incomingOrder) {
    return new OutgoingOrder(
        incomingOrder.id(),
        incomingOrder.productId(),
        determineProductCategory(incomingOrder.productId()));
  }

  private static ProductCategory determineProductCategory(long productId) {
    if (productId < 100) {
      return ProductCategory.ELECTRONICS;
    } else if (productId > 100 && productId < 200) {
      return ProductCategory.BOOKS;
    }
    return ProductCategory.FOOD;
  }
}
