package com.giansantos.helpdesk.ticket.internal.domain;

import com.giansantos.helpdesk.ticket.api.TicketStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tickets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ticket {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Column(nullable = false)
    private UUID requesterId;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant closedAt;

    public static Ticket create(String subject, UUID requesterId) {
        Ticket ticket = new Ticket();
        ticket.id = UUID.randomUUID();
        ticket.subject = subject;
        ticket.status = TicketStatus.OPEN;
        ticket.requesterId = requesterId;
        ticket.createdAt = Instant.now();
        return ticket;
    }

    public void close() {
        if (this.status == TicketStatus.CLOSED) {
            return;
        }
        this.status = TicketStatus.CLOSED;
        this.closedAt = Instant.now();
    }
}
