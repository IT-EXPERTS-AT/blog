package at.itexperts.cinema.ticketing;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
@Validated
public class TicketApi {

    private final TicketingService ticketingService;

    public TicketApi(final TicketingService ticketingService) {
        this.ticketingService = ticketingService;
    }

    @PostMapping
    public void buyTicket(@RequestBody final BuyTicketRequest buyTicketRequest) {
        ticketingService.buyTicket(new Ticket(buyTicketRequest.showId()));
    }
}
