package com.giansantos.helpdesk.ticket.api;

import java.time.Instant;
import java.util.UUID;

public record TicketSummary(UUID id, String subject, TicketStatus status, UUID requesterId, Instant createdAt) {
}
