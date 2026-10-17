package com.giansantos.helpdesk.ticket.internal.repo;

import com.giansantos.helpdesk.ticket.internal.domain.TicketMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketMessageRepository extends JpaRepository<TicketMessage, UUID> {
    List<TicketMessage> findByTicketIdOrderByCreatedAtDesc(UUID ticketId, Pageable pageable);
}
