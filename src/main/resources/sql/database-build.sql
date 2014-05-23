CREATE TABLE public.tickets (
    id IDENTITY NOT NULL,
    detail_message VARCHAR(250),
    stacktrace CLOB,
    ticket_class_id INT NOT NULL DEFAULT 13,
    PRIMARY KEY  (id)
);

CREATE TABLE public.ticket_occurrences (
    id IDENTITY NOT NULL,
    occurrence_timestamp TIMESTAMP NOT NULL,
    ticket_id BIGINT, 
    PRIMARY KEY (id),
    FOREIGN KEY (ticket_id) REFERENCES tickets (id)
);