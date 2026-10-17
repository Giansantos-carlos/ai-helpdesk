package com.giansantos.helpdesk.ticket.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateTicketRequest(@NotBlank String subject, @NotNull UUID requesterId) {
}
