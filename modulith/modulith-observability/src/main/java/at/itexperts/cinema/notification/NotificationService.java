package at.itexperts.cinema.notification;

import static at.itexperts.cinema.generated.Sequences.NOTIFICATION_SEQ;

import at.itexperts.cinema.generated.Tables;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NotificationService {

    private final DSLContext dsl;

    public NotificationService(final DSLContext dsl) {
        this.dsl = dsl;
    }

    public void addTicketPurchaseNotification() {
        dsl.insertInto(Tables.NOTIFICATION)
                .values(NOTIFICATION_SEQ.nextval(), "TICKET")
                .execute();
    }
}
