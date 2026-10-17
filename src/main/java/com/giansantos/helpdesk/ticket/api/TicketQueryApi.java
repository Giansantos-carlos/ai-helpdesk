package com.giansantos.helpdesk.ticket.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Superficie publica do modulo ticket para leitura, usada por outros modulos (ex: assistant). */
public interface TicketQueryApi {
    Optional<TicketSummary> findTicket(UUID ticketId);

    List<TicketMessageView> findRecentMessages(UUID ticketId, int maxMessages);
}
