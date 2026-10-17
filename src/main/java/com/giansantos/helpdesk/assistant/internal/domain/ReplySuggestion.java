package com.giansantos.helpdesk.assistant.internal.domain;

import com.giansantos.helpdesk.assistant.api.SuggestionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/** Sem FK fisica para "tickets": modulo assistant nao compartilha schema com o modulo ticket. */
@Entity
@Table(name = "reply_suggestions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReplySuggestion {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID ticketId;

    @Column(nullable = false, columnDefinition = "text")
    private String promptContext;

    @Column(columnDefinition = "text")
    private String suggestedReply;

    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SuggestionStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    public static ReplySuggestion pending(UUID ticketId, String promptContext) {
        ReplySuggestion suggestion = new ReplySuggestion();
        suggestion.id = UUID.randomUUID();
        suggestion.ticketId = ticketId;
        suggestion.promptContext = promptContext;
        suggestion.status = SuggestionStatus.PENDING;
        suggestion.createdAt = Instant.now();
        return suggestion;
    }

    public void markReady(String suggestedReply, String model) {
        this.suggestedReply = suggestedReply;
        this.model = model;
        this.status = SuggestionStatus.READY;
    }

    public void markFailed() {
        this.status = SuggestionStatus.FAILED;
    }
}
