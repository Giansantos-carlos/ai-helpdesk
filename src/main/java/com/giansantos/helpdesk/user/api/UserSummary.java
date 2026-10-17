package com.giansantos.helpdesk.user.api;

import java.util.UUID;

/** Visao minima de um usuario, exposta a outros modulos (ex: assistant monta tom pelo perfil). */
public record UserSummary(UUID id, String email, String displayName, UserRole role) {
}
