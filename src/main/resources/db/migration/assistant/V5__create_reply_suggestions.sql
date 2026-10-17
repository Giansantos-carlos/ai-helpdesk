-- Sem FK fisica para "tickets": modulo assistant nao compartilha schema com o modulo ticket.
CREATE TABLE reply_suggestions (
    id UUID PRIMARY KEY,
    ticket_id UUID NOT NULL,
    prompt_context TEXT NOT NULL,
    suggested_reply TEXT,
    model VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_reply_suggestions_ticket_id ON reply_suggestions (ticket_id);
