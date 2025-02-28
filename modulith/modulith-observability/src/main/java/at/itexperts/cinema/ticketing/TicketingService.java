package at.itexperts.cinema.ticketing;

import static at.itexperts.cinema.generated.Sequences.TICKET_SEQ;

import at.itexperts.cinema.generated.Tables;
import at.itexperts.cinema.notification.NotificationService;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TicketingService {

    private final DSLContext dsl;
    private final NotificationService notificationService;

    public TicketingService(final DSLContext dsl, final NotificationService notificationService) {
        this.dsl = dsl;
        this.notificationService = notificationService;
    }

    @Transactional
    public void buyTicket(final Ticket ticket) {
        dsl.insertInto(Tables.TICKET)
                .values(TICKET_SEQ.nextval(), ticket.showId())
                .execute();
        notificationService.addTicketPurchaseNotification();
    }
}
