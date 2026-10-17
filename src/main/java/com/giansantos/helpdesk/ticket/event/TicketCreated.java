package com.giansantos.helpdesk.ticket.event;

import java.time.Instant;
import java.util.UUID;

public record TicketCreated(UUID ticketId, String subject, UUID requesterId, Instant createdAt) {
}
