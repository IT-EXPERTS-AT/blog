package at.itexperts.cinema.notification;

import static at.itexperts.cinema.generated.Sequences.NOTIFICATION_SEQ;
import static org.jooq.impl.DSL.count;

import at.itexperts.cinema.generated.Tables;
import org.jooq.DSLContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NotificationService {

    private final DSLContext dsl;

    public NotificationService(final DSLContext dsl) {
        this.dsl = dsl;
    }

    @Transactional
    public void addTicketPurchaseNotification() {
        dsl.insertInto(Tables.NOTIFICATION)
                .values(NOTIFICATION_SEQ.nextval(), "TICKET")
                .execute();
    }

    @Scheduled(fixedDelayString = "PT1M")
    public void countTicketPurchaseNotification() {
        dsl.select(count()).from(Tables.NOTIFICATION).execute();
    }
}
