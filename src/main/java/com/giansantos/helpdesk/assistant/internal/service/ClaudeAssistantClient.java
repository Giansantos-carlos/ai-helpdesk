package com.giansantos.helpdesk.assistant.internal.service;

import com.giansantos.helpdesk.assistant.config.AnthropicProperties;
import com.giansantos.helpdesk.shared.exception.AiAnalysisException;
import com.giansantos.helpdesk.shared.exception.AiNotConfiguredException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

/** Cliente para a API de Messages da Anthropic (Claude), usado para sugerir respostas de atendente. */
@Component
public class ClaudeAssistantClient {

    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final RestClient restClient;
    private final AnthropicProperties properties;

    public ClaudeAssistantClient(AnthropicProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder().baseUrl(properties.baseUrl()).build();
    }

    public String suggestReply(ContextBundle context) {
        if (!StringUtils.hasText(properties.apiKey())) {
            throw new AiNotConfiguredException(
                    "ANTHROPIC_API_KEY nao configurada. Defina a variavel de ambiente para habilitar sugestoes de IA.");
        }

        String prompt = PromptBuilder.build(context);
        Map<String, Object> requestBody = Map.of(
                "model", properties.model(),
                "max_tokens", properties.maxOutputTokens(),
                "messages", List.of(Map.of("role", "user", "content", prompt)));

        Map<String, Object> response;
        try {
            response = restClient.post()
                    .uri("/messages")
                    .header("x-api-key", properties.apiKey())
                    .header("anthropic-version", ANTHROPIC_VERSION)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
        } catch (RestClientResponseException ex) {
            throw new AiAnalysisException(
                    "Falha ao chamar a API da Anthropic: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString(), ex);
        }

        return extractText(response).strip();
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> response) {
        Object content = response == null ? null : response.get("content");
        if (content instanceof List<?> blocks && !blocks.isEmpty() && blocks.get(0) instanceof Map<?, ?> block) {
            Object text = block.get("text");
            if (text != null) {
                return text.toString();
            }
        }
        throw new AiAnalysisException("Resposta inesperada da API da Anthropic: " + response);
    }
}
