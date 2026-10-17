# Spec: assistant

## Status
Approved

## Contexto
Este é o módulo que demonstra o pilar de **Context Engineering**: quando um ticket é criado, o
sistema deve montar automaticamente um contexto relevante (histórico do ticket + artigos de KB +
perfil do requester) e otimizá-lo em tempo real — rankear o que é mais relevante e truncar contra
um orçamento de tokens — antes de enviá-lo a um modelo de linguagem (Claude) para gerar uma
sugestão de resposta ao atendente. A qualidade dessa montagem de contexto é o que diferencia uma
chamada de IA "genérica" de uma que usa engenharia de contexto de fato.

## Goals
- Reagir a `TicketCreated` (publicado pelo módulo `ticket`) de forma assíncrona, sem acoplar o
  `ticket` ao `assistant`.
- Montar um `ContextBundle`: histórico de mensagens do ticket (via API pública do `ticket`),
  top-N artigos de KB relevantes (via API pública do `knowledgebase`) e perfil do requester (via
  API pública do `user`).
- Rankear os artigos de KB por relevância textual e truncar histórico + artigos contra um
  orçamento de caracteres/tokens configurável, descartando os itens de menor score quando
  necessário.
- Chamar a API de Messages da Anthropic com o contexto montado e persistir a sugestão gerada.
- Expor a sugestão via `GET /api/tickets/{ticketId}/suggestion`.

## Non-Goals
- Geração de resposta síncrona no momento da criação do ticket (o processamento é assíncrono;
  o atendente pode encontrar "ainda não gerado" nos primeiros segundos — ver Riscos).
- Tokenização real (contagem exata de tokens do modelo). Usa heurística char/4.
- Reenvio automático de eventos perdidos após restart (sem outbox durável).
- Edição manual da sugestão pelo atendente (fica para um módulo futuro).

## Requirements
1. Ao receber `TicketCreated`, o `TicketEventListener` dispara `AssistantService.generateSuggestion`
   de forma assíncrona (virtual thread), sem bloquear o fluxo de criação do ticket.
2. `ContextBuilder.build(ticketId)` busca histórico de mensagens (últimas K, configurável),
   busca KB por palavras-chave extraídas do assunto/primeira mensagem (até N artigos,
   configurável) e busca o perfil do requester.
3. `ContextBuilder` rankeia artigos de KB por score de sobreposição de palavras-chave e trunca a
   soma de (histórico + artigos) contra `app.assistant.context.max-chars-budget`, removendo
   primeiro os itens de menor score até caber no orçamento.
4. `ClaudeAssistantClient` chama `POST {baseUrl}/messages` da Anthropic; se `ANTHROPIC_API_KEY`
   não estiver configurada, lança `AiNotConfiguredException` e o sistema degrada graciosamente
   (sem sugestão, sem erro 500).
5. `GET /api/tickets/{ticketId}/suggestion` retorna a sugestão mais recente ou 404 se ainda não
   foi gerada (ou falhou).

## API Contract

| Method | Path | Request | Response | Notes |
|---|---|---|---|---|
| GET | /api/tickets/{ticketId}/suggestion | — | `SuggestionResponse(status, suggestedReply, model, createdAt)` (200) | 404 se não existir ainda |

## Data Model
- `assistant.reply_suggestions`: `id (uuid, pk)`, `ticket_id (uuid, not null, sem FK física)`, `prompt_context (text, not null)`, `suggested_reply (text, nullable)`, `model (varchar, nullable)`, `status (varchar, not null)`, `created_at (timestamp, not null)`.

## Events Published / Consumed
- Publishes: (nenhum)
- Consumes: `TicketCreated` (do módulo `ticket`)

## Acceptance Criteria
- [ ] Dado um ticket recém-criado, quando o listener processa `TicketCreated`, então uma linha
      `PENDING` é criada e, após a chamada à IA, atualizada para `READY` (com `suggestedReply`)
      ou `FAILED`.
- [ ] Dado um orçamento de caracteres pequeno e muitos artigos de KB relevantes, quando
      `ContextBuilder.build` monta o contexto, então apenas os artigos de maior score entram,
      respeitando o orçamento.
- [ ] Dado `ANTHROPIC_API_KEY` ausente, quando o listener processa o evento, então a sugestão
      fica `FAILED` sem exceção não tratada subindo ao chamador.
- [ ] Dado nenhuma sugestão gerada ainda, quando `GET /api/tickets/{id}/suggestion`, então
      retorna 404 (e não um 200 com corpo vazio).

## Riscos / Simplificações
- Processamento assíncrono: existe uma janela de tempo em que a sugestão legitimamente não
  existe ainda (eventual consistency), o que é esperado e não um bug.
- Orçamento de contexto é uma heurística char/4, não a tokenização real do modelo Claude.
- Sem outbox durável — eventos perdidos por restart do processo não são reprocessados
  automaticamente neste projeto (contraste deliberado com o padrão de outbox usado no projeto
  irmão `event-order-system`, citado no README raiz).
