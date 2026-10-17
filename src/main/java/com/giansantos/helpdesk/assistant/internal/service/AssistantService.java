package com.giansantos.helpdesk.assistant.internal.service;

import com.giansantos.helpdesk.assistant.internal.domain.ReplySuggestion;
import com.giansantos.helpdesk.assistant.internal.repo.ReplySuggestionRepository;
import com.giansantos.helpdesk.shared.exception.AiAnalysisException;
import com.giansantos.helpdesk.shared.exception.AiNotConfiguredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class AssistantService {

    private static final Logger log = LoggerFactory.getLogger(AssistantService.class);

    private final ContextBuilder contextBuilder;
    private final ClaudeAssistantClient claudeAssistantClient;
    private final ReplySuggestionRepository suggestionRepository;

    public AssistantService(ContextBuilder contextBuilder, ClaudeAssistantClient claudeAssistantClient,
                             ReplySuggestionRepository suggestionRepository) {
        this.contextBuilder = contextBuilder;
        this.claudeAssistantClient = claudeAssistantClient;
        this.suggestionRepository = suggestionRepository;
    }

    @Transactional
    public void generateSuggestion(UUID ticketId) {
        ContextBundle context = contextBuilder.build(ticketId);
        ReplySuggestion suggestion = suggestionRepository.save(
                ReplySuggestion.pending(ticketId, PromptBuilder.build(context)));

        try {
            String reply = claudeAssistantClient.suggestReply(context);
            suggestion.markReady(reply, "claude");
        } catch (AiNotConfiguredException | AiAnalysisException ex) {
            log.warn("Nao foi possivel gerar sugestao de IA para o ticket {}: {}", ticketId, ex.getMessage());
            suggestion.markFailed();
        }
    }

    @Transactional(readOnly = true)
    public Optional<ReplySuggestion> findLatest(UUID ticketId) {
        return suggestionRepository.findFirstByTicketIdOrderByCreatedAtDesc(ticketId);
    }
}
