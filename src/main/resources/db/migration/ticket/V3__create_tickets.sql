CREATE TABLE tickets (
    id UUID PRIMARY KEY,
    subject VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    requester_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    closed_at TIMESTAMP
);
