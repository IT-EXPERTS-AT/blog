/*
 * This file is generated by jOOQ.
 */
package at.itexperts.cinema.generated;

import at.itexperts.cinema.generated.tables.Movie;
import at.itexperts.cinema.generated.tables.Notification;
import at.itexperts.cinema.generated.tables.Show;
import at.itexperts.cinema.generated.tables.Ticket;
import java.util.Arrays;
import java.util.List;
import org.jooq.Catalog;
import org.jooq.Sequence;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;

/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes", "this-escape"})
public class Public extends SchemaImpl {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public</code>
     */
    public static final Public PUBLIC = new Public();

    /**
     * The table <code>public.movie</code>.
     */
    public final Movie MOVIE = Movie.MOVIE;

    /**
     * The table <code>public.notification</code>.
     */
    public final Notification NOTIFICATION = Notification.NOTIFICATION;

    /**
     * The table <code>public.show</code>.
     */
    public final Show SHOW = Show.SHOW;

    /**
     * The table <code>public.ticket</code>.
     */
    public final Ticket TICKET = Ticket.TICKET;

    /**
     * No further instances allowed
     */
    private Public() {
        super("public", null);
    }

    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Sequence<?>> getSequences() {
        return Arrays.asList(Sequences.MOVIE_SEQ, Sequences.NOTIFICATION_SEQ, Sequences.SHOW_SEQ, Sequences.TICKET_SEQ);
    }

    @Override
    public final List<Table<?>> getTables() {
        return Arrays.asList(Movie.MOVIE, Notification.NOTIFICATION, Show.SHOW, Ticket.TICKET);
    }
}
