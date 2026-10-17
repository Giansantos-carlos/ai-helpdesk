package com.giansantos.helpdesk.ticket.api;

import com.giansantos.helpdesk.ticket.internal.domain.Ticket;

import java.time.Instant;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        String subject,
        TicketStatus status,
        UUID requesterId,
        Instant createdAt,
        Instant closedAt) {

    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getSubject(),
                ticket.getStatus(),
                ticket.getRequesterId(),
                ticket.getCreatedAt(),
                ticket.getClosedAt());
    }
}
