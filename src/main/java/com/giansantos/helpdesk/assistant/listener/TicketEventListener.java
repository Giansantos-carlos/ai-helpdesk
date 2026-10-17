package com.giansantos.helpdesk.assistant.listener;

import com.giansantos.helpdesk.assistant.internal.service.AssistantService;
import com.giansantos.helpdesk.ticket.event.TicketCreated;
import org.springframework.modulith.events.ApplicationModuleListener;

/**
 * Reage a {@code TicketCreated} do modulo ticket de forma assincrona e desacoplada: o modulo
 * ticket nao conhece o assistant, apenas publica o evento. O listener roda apos o commit da
 * transacao que criou o ticket (ver ApplicationModuleListener), em uma virtual thread
 * (spring.threads.virtual.enabled=true), sem bloquear a requisicao HTTP original.
 */
@org.springframework.stereotype.Component
public class TicketEventListener {

    private final AssistantService assistantService;

    public TicketEventListener(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @ApplicationModuleListener
    void onTicketCreated(TicketCreated event) {
        assistantService.generateSuggestion(event.ticketId());
    }
}
