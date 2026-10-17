package com.giansantos.helpdesk.user.api;

import java.util.Optional;
import java.util.UUID;

/** Superficie publica do modulo user para leitura, usada por outros modulos (ex: assistant). */
public interface UserQueryApi {
    Optional<UserSummary> findSummary(UUID userId);
}
