package com.giansantos.helpdesk.ticket.internal.service;

import com.giansantos.helpdesk.shared.exception.ResourceNotFoundException;
import com.giansantos.helpdesk.ticket.api.TicketMessageView;
import com.giansantos.helpdesk.ticket.api.TicketQueryApi;
import com.giansantos.helpdesk.ticket.api.TicketStatus;
import com.giansantos.helpdesk.ticket.api.TicketSummary;
import com.giansantos.helpdesk.ticket.event.TicketCreated;
import com.giansantos.helpdesk.ticket.event.TicketMessageAdded;
import com.giansantos.helpdesk.ticket.internal.domain.Ticket;
import com.giansantos.helpdesk.ticket.internal.domain.TicketMessage;
import com.giansantos.helpdesk.ticket.internal.repo.TicketMessageRepository;
import com.giansantos.helpdesk.ticket.internal.repo.TicketRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TicketService implements TicketQueryApi {

    private final TicketRepository ticketRepository;
    private final TicketMessageRepository messageRepository;
    private final ApplicationEventPublisher events;

    public TicketService(TicketRepository ticketRepository, TicketMessageRepository messageRepository,
                          ApplicationEventPublisher events) {
        this.ticketRepository = ticketRepository;
        this.messageRepository = messageRepository;
        this.events = events;
    }

    public Ticket createTicket(String subject, UUID requesterId) {
        Ticket ticket = ticketRepository.save(Ticket.create(subject, requesterId));
        events.publishEvent(new TicketCreated(ticket.getId(), ticket.getSubject(), ticket.getRequesterId(), ticket.getCreatedAt()));
        return ticket;
    }

    public Ticket addMessage(UUID ticketId, UUID authorId, String body) {
        Ticket ticket = getTicketOrThrow(ticketId);
        TicketMessage message = messageRepository.save(TicketMessage.create(ticketId, authorId, body));
        events.publishEvent(new TicketMessageAdded(ticketId, message.getId(), authorId, body, message.getCreatedAt()));
        return ticket;
    }

    public Ticket closeTicket(UUID ticketId) {
        Ticket ticket = getTicketOrThrow(ticketId);
        ticket.close();
        return ticket;
    }

    @Transactional(readOnly = true)
    public List<Ticket> listTickets(TicketStatus status) {
        return status == null
                ? ticketRepository.findAllByOrderByCreatedAtDesc()
                : ticketRepository.findAllByStatusOrderByCreatedAtDesc(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TicketSummary> findTicket(UUID ticketId) {
        return ticketRepository.findById(ticketId)
                .map(t -> new TicketSummary(t.getId(), t.getSubject(), t.getStatus(), t.getRequesterId(), t.getCreatedAt()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketMessageView> findRecentMessages(UUID ticketId, int maxMessages) {
        var pageable = PageRequest.of(0, maxMessages, Sort.by("createdAt").descending());
        return messageRepository.findByTicketIdOrderByCreatedAtDesc(ticketId, pageable).stream()
                .map(m -> new TicketMessageView(m.getId(), m.getAuthorId(), m.getBody(), m.getCreatedAt()))
                .toList();
    }

    private Ticket getTicketOrThrow(UUID ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket nao encontrado: " + ticketId));
    }
}
