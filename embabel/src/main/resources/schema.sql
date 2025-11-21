CREATE TABLE IF NOT EXISTS course (
    id SERIAL PRIMARY KEY,
    sport TEXT,
    level TEXT,
    duration_in_hours INT,
    location TEXT,
    marketing_text TEXT
);

