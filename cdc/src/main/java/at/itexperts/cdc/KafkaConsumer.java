package at.itexperts.cdc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {
    private Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

    @KafkaListener(topics = "dbserver1.public.person")
    public void listenToPersonChanges(String message) {
        logger.info("Received change message: {}", message);
    }
}
