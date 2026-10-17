package com.giansantos.helpdesk.ticket.api;

import java.time.Instant;
import java.util.UUID;

public record TicketMessageView(UUID id, UUID authorId, String body, Instant createdAt) {
}
