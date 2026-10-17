package com.giansantos.helpdesk.assistant.api;

import com.giansantos.helpdesk.assistant.internal.service.AssistantService;
import com.giansantos.helpdesk.shared.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/tickets/{ticketId}/suggestion")
public class SuggestionController {

    private final AssistantService assistantService;

    public SuggestionController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @GetMapping
    public SuggestionResponse getSuggestion(@PathVariable UUID ticketId) {
        return assistantService.findLatest(ticketId)
                .map(SuggestionResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nenhuma sugestao gerada ainda para o ticket " + ticketId));
    }
}
