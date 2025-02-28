CREATE SEQUENCE notification_seq START 1;

CREATE TABLE notification
(
    id    BIGINT PRIMARY KEY DEFAULT nextval('movie_seq'),
    type  VARCHAR(255) NOT NULL
);