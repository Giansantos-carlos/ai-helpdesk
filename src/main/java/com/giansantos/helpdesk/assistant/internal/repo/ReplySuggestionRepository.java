package com.giansantos.helpdesk.assistant.internal.repo;

import com.giansantos.helpdesk.assistant.internal.domain.ReplySuggestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReplySuggestionRepository extends JpaRepository<ReplySuggestion, UUID> {
    Optional<ReplySuggestion> findFirstByTicketIdOrderByCreatedAtDesc(UUID ticketId);
}
