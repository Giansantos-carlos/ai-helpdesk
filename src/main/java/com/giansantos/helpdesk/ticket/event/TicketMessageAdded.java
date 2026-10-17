package com.giansantos.helpdesk.ticket.event;

import java.time.Instant;
import java.util.UUID;

public record TicketMessageAdded(UUID ticketId, UUID messageId, UUID authorId, String body, Instant createdAt) {
}
