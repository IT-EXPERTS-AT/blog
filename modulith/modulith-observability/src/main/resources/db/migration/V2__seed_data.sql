INSERT INTO movie (id, title)
VALUES (nextval('movie_seq'), 'Interstellar'),
       (nextval('movie_seq'), 'Joker');

INSERT INTO show (id, movie_id, theater_name, show_time)
VALUES (nextval('show_seq'), 1, 'VIP Hall', now() + interval '5 hour'),
       (nextval('show_seq'), 2, 'IMAX', now() + interval '8 hour');