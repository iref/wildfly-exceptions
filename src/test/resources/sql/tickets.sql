INSERT INTO tickets(id, detail_message, class_name, stacktrace, ticket_class_id)
VALUES (10, 'Something went horribly wrong', 'OctocatException', 'StackTrace1', 1);

INSERT INTO tickets(id, detail_message, class_name, stacktrace, ticket_class_id)
VALUES (20, 'Something went even more wrong', 'UnicornException', 'StackTrace2', 2);

INSERT INTO ticket_occurrences(id, occurrence_timestamp, ticket_id) VALUES (100,'2014-05-05 12:34:25.234', 10);
INSERT INTO ticket_occurrences(id, occurrence_timestamp, ticket_id) VALUES (200, '2014-05-06 12:23:12.456', 10);