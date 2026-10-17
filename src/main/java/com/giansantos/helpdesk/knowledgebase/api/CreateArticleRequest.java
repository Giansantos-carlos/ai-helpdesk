package com.giansantos.helpdesk.knowledgebase.api;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateArticleRequest(@NotBlank String title, @NotBlank String body, List<String> tags) {

    public CreateArticleRequest {
        tags = tags == null ? List.of() : List.copyOf(tags);
    }
}
