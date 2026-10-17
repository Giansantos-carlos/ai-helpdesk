package com.giansantos.helpdesk.ticket.api;

import com.giansantos.helpdesk.ticket.internal.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        var ticket = ticketService.createTicket(request.subject(), request.requesterId());
        return ResponseEntity.status(HttpStatus.CREATED).body(TicketResponse.from(ticket));
    }

    @PostMapping("/{id}/messages")
    public TicketResponse addMessage(@PathVariable UUID id, @Valid @RequestBody AddMessageRequest request) {
        var ticket = ticketService.addMessage(id, request.authorId(), request.body());
        return TicketResponse.from(ticket);
    }

    @PostMapping("/{id}/close")
    public TicketResponse closeTicket(@PathVariable UUID id) {
        return TicketResponse.from(ticketService.closeTicket(id));
    }

    @GetMapping
    public List<TicketResponse> listTickets(@RequestParam(required = false) TicketStatus status) {
        return ticketService.listTickets(status).stream().map(TicketResponse::from).toList();
    }
}
