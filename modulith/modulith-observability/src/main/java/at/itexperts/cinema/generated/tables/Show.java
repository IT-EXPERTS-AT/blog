/*
 * This file is generated by jOOQ.
 */
package at.itexperts.cinema.generated.tables;

import at.itexperts.cinema.generated.Keys;
import at.itexperts.cinema.generated.Public;
import at.itexperts.cinema.generated.tables.Movie.MoviePath;
import at.itexperts.cinema.generated.tables.Ticket.TicketPath;
import at.itexperts.cinema.generated.tables.records.ShowRecord;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.InverseForeignKey;
import org.jooq.Name;
import org.jooq.Path;
import org.jooq.PlainSQL;
import org.jooq.QueryPart;
import org.jooq.Record;
import org.jooq.SQL;
import org.jooq.Schema;
import org.jooq.Select;
import org.jooq.Stringly;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes", "this-escape"})
public class Show extends TableImpl<ShowRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.show</code>
     */
    public static final Show SHOW = new Show();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ShowRecord> getRecordType() {
        return ShowRecord.class;
    }

    /**
     * The column <code>public.show.id</code>.
     */
    public final TableField<ShowRecord, Long> ID =
            createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.show.movie_id</code>.
     */
    public final TableField<ShowRecord, Long> MOVIE_ID =
            createField(DSL.name("movie_id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.show.theater_name</code>.
     */
    public final TableField<ShowRecord, String> THEATER_NAME =
            createField(DSL.name("theater_name"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>public.show.show_time</code>.
     */
    public final TableField<ShowRecord, LocalDateTime> SHOW_TIME =
            createField(DSL.name("show_time"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "");

    private Show(Name alias, Table<ShowRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private Show(Name alias, Table<ShowRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.show</code> table reference
     */
    public Show(String alias) {
        this(DSL.name(alias), SHOW);
    }

    /**
     * Create an aliased <code>public.show</code> table reference
     */
    public Show(Name alias) {
        this(alias, SHOW);
    }

    /**
     * Create a <code>public.show</code> table reference
     */
    public Show() {
        this(DSL.name("show"), null);
    }

    public <O extends Record> Show(
            Table<O> path, ForeignKey<O, ShowRecord> childPath, InverseForeignKey<O, ShowRecord> parentPath) {
        super(path, childPath, parentPath, SHOW);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class ShowPath extends Show implements Path<ShowRecord> {

        private static final long serialVersionUID = 1L;

        public <O extends Record> ShowPath(
                Table<O> path, ForeignKey<O, ShowRecord> childPath, InverseForeignKey<O, ShowRecord> parentPath) {
            super(path, childPath, parentPath);
        }

        private ShowPath(Name alias, Table<ShowRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public ShowPath as(String alias) {
            return new ShowPath(DSL.name(alias), this);
        }

        @Override
        public ShowPath as(Name alias) {
            return new ShowPath(alias, this);
        }

        @Override
        public ShowPath as(Table<?> alias) {
            return new ShowPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<ShowRecord, Long> getIdentity() {
        return (Identity<ShowRecord, Long>) super.getIdentity();
    }

    @Override
    public UniqueKey<ShowRecord> getPrimaryKey() {
        return Keys.SHOW_PKEY;
    }

    @Override
    public List<ForeignKey<ShowRecord, ?>> getReferences() {
        return Arrays.asList(Keys.SHOW__SHOW_MOVIE_ID_FKEY);
    }

    private transient MoviePath _movie;

    /**
     * Get the implicit join path to the <code>public.movie</code> table.
     */
    public MoviePath movie() {
        if (_movie == null) _movie = new MoviePath(this, Keys.SHOW__SHOW_MOVIE_ID_FKEY, null);

        return _movie;
    }

    private transient TicketPath _ticket;

    /**
     * Get the implicit to-many join path to the <code>public.ticket</code>
     * table
     */
    public TicketPath ticket() {
        if (_ticket == null) _ticket = new TicketPath(this, null, Keys.TICKET__TICKET_SHOW_ID_FKEY.getInverseKey());

        return _ticket;
    }

    @Override
    public Show as(String alias) {
        return new Show(DSL.name(alias), this);
    }

    @Override
    public Show as(Name alias) {
        return new Show(alias, this);
    }

    @Override
    public Show as(Table<?> alias) {
        return new Show(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Show rename(String name) {
        return new Show(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Show rename(Name name) {
        return new Show(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Show rename(Table<?> name) {
        return new Show(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Show where(Condition condition) {
        return new Show(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Show where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Show where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Show where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Show where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Show where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Show where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Show where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Show whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Show whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
