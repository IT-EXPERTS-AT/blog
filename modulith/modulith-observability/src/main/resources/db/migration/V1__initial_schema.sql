CREATE SEQUENCE movie_seq START 1;
CREATE SEQUENCE show_seq START 1;
CREATE SEQUENCE ticket_seq START 1;

CREATE TABLE movie
(
    id    BIGINT PRIMARY KEY DEFAULT nextval('movie_seq'),
    title VARCHAR(255) NOT NULL
);

CREATE TABLE show
(
    id           BIGINT PRIMARY KEY DEFAULT nextval('show_seq'),
    movie_id     BIGINT       NOT NULL,
    theater_name VARCHAR(255) NOT NULL,
    show_time    TIMESTAMP    NOT NULL,
    FOREIGN KEY (movie_id) REFERENCES movie (id)
);

CREATE TABLE ticket
(
    id      BIGINT PRIMARY KEY DEFAULT nextval('ticket_seq'),
    show_id BIGINT NOT NULL,
    FOREIGN KEY (show_id) REFERENCES show (id)
);