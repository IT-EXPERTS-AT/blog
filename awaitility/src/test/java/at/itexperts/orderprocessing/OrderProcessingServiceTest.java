package at.itexperts.orderprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.*;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;

@SpringBootTest
class OrderProcessingServiceTest {

  private static final Duration WAIT_DURATION = Duration.of(5, ChronoUnit.SECONDS);

  @Autowired private AmqpTemplate amqpTemplate;
  @Autowired private AmqpAdmin amqpAdmin;
  @Autowired private OrderProcessingService orderProcessingService;

  @BeforeEach
  void setUp() {
    amqpAdmin.purgeQueue(OrderProcessingService.INCOMING_ORDER_QUEUE);
    amqpAdmin.purgeQueue(OrderProcessingService.OUTGOING_ORDER_QUEUE);
  }

  @Test
  void shouldAddOrderToOutgoingQueue() {
    int initialMessageCount =
        amqpAdmin.getQueueInfo(OrderProcessingService.OUTGOING_ORDER_QUEUE).getMessageCount();

    sendOrderToIncomingQueue(1);

    waitAtMost(WAIT_DURATION)
        .until(
            () ->
                amqpAdmin
                        .getQueueInfo(OrderProcessingService.OUTGOING_ORDER_QUEUE)
                        .getMessageCount()
                    > initialMessageCount);
  }

  @Test
  void shouldAddOrderToOutgoingQueue_V2() {
    sendOrderToIncomingQueue(1);

    waitAtMost(WAIT_DURATION)
        .until(
            () ->
                amqpTemplate.receiveAndConvert(
                        OrderProcessingService.OUTGOING_ORDER_QUEUE,
                        ParameterizedTypeReference.forType(OutgoingOrder.class))
                    != null);
  }

  @Test
  @Disabled("not working due to no wait")
  void checkOrderFlagWithoutAwaitility() {
    long orderId = new Random().nextLong();

    sendOrderToIncomingQueue(orderId);

    assertThat(orderProcessingService.wasOrderProcessed(orderId)).isTrue();
  }

  @Test
  void checkOrderFlagWithAwaitility() {
    long orderId = new Random().nextLong();

    sendOrderToIncomingQueue(orderId);

    waitAtMost(WAIT_DURATION).until(() -> orderProcessingService.wasOrderProcessed(orderId));
  }

  private void sendOrderToIncomingQueue(long orderId) {
    amqpTemplate.convertAndSend(
        OrderProcessingService.ORDER_EXCHANGE,
        OrderProcessingService.INCOMING_ORDER_ROUTING_KEY,
        new IncomingOrder(orderId, 1));
  }
}
