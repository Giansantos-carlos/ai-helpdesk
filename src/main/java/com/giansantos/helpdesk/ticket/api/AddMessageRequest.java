package com.giansantos.helpdesk.ticket.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddMessageRequest(@NotNull UUID authorId, @NotBlank String body) {
}
