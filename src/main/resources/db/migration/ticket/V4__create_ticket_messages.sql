-- Sem FK fisica para "tickets": modulos ticket e demais nao compartilham integridade de schema.
CREATE TABLE ticket_messages (
    id UUID PRIMARY KEY,
    ticket_id UUID NOT NULL,
    author_id UUID NOT NULL,
    body TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_ticket_messages_ticket_id ON ticket_messages (ticket_id);
