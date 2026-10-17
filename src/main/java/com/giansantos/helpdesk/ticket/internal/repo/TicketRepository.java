package com.giansantos.helpdesk.ticket.internal.repo;

import com.giansantos.helpdesk.ticket.api.TicketStatus;
import com.giansantos.helpdesk.ticket.internal.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findAllByOrderByCreatedAtDesc();

    List<Ticket> findAllByStatusOrderByCreatedAtDesc(TicketStatus status);
}
