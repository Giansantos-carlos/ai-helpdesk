package com.giansantos.helpdesk.ticket.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/** Sem FK fisica para {@code tickets}: a relacao e mantida apenas pelo valor de {@code ticketId}. */
@Entity
@Table(name = "ticket_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketMessage {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID ticketId;

    @Column(nullable = false)
    private UUID authorId;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    @Column(nullable = false)
    private Instant createdAt;

    public static TicketMessage create(UUID ticketId, UUID authorId, String body) {
        TicketMessage message = new TicketMessage();
        message.id = UUID.randomUUID();
        message.ticketId = ticketId;
        message.authorId = authorId;
        message.body = body;
        message.createdAt = Instant.now();
        return message;
    }
}
