CREATE SEQUENCE IF NOT EXISTS tickets_id_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS ticket_occurrences_id_seq START WITH 1;

CREATE TABLE public.tickets (
    id BIGINT DEFAULT NEXTVAL('tickets_id_seq') NOT NULL,
    detail_message VARCHAR(1024),
    class_name VARCHAR(1024),
    stacktrace CLOB,
    ticket_class_id INT NOT NULL DEFAULT 13,
    PRIMARY KEY  (id)
);

CREATE TABLE public.ticket_occurrences (
    id BIGINT DEFAULT NEXTVAL('ticket_occurrences_id_seq') NOT NULL,
    occurrence_timestamp TIMESTAMP NOT NULL,
    ticket_id BIGINT, 
    PRIMARY KEY (id),
    FOREIGN KEY (ticket_id) REFERENCES tickets (id)
);