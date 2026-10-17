package com.giansantos.helpdesk.assistant.api;

import com.giansantos.helpdesk.assistant.internal.domain.ReplySuggestion;

import java.time.Instant;

public record SuggestionResponse(SuggestionStatus status, String suggestedReply, String model, Instant createdAt) {

    public static SuggestionResponse from(ReplySuggestion suggestion) {
        return new SuggestionResponse(suggestion.getStatus(), suggestion.getSuggestedReply(), suggestion.getModel(),
                suggestion.getCreatedAt());
    }
}
