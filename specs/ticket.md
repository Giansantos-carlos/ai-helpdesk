# Spec: ticket

## Status
Approved

## Contexto
É o módulo central do domínio: representa o ciclo de vida de um chamado de suporte. Precisa
publicar eventos de domínio (`TicketCreated`, `TicketMessageAdded`) para que o módulo
`assistant` reaja de forma assíncrona e desacoplada — este módulo não conhece o `assistant` e
não deve ser modificado quando a lógica de IA mudar.

## Goals
- Criar um ticket (assunto + requester).
- Adicionar mensagens a um ticket (histórico da conversa).
- Fechar um ticket.
- Listar tickets, opcionalmente filtrando por status.
- Publicar eventos de domínio em cada mudança relevante de estado.

## Non-Goals
- Atribuição automática de atendente (fila, SLA, prioridade).
- Edição/remoção de mensagens.
- Reabertura de ticket fechado.

## Requirements
1. `POST /api/tickets` cria um ticket com status `OPEN` e publica `TicketCreated`.
2. `POST /api/tickets/{id}/messages` adiciona uma mensagem (autor + corpo) e publica
   `TicketMessageAdded`; falha com 404 se o ticket não existir.
3. `POST /api/tickets/{id}/close` muda o status para `CLOSED` e grava `closedAt`; é idempotente
   (fechar um ticket já fechado não é erro).
4. `GET /api/tickets?status=OPEN` lista tickets, com filtro opcional de status.

## API Contract

| Method | Path | Request | Response | Notes |
|---|---|---|---|---|
| POST | /api/tickets | `CreateTicketRequest(subject, requesterId)` | `TicketResponse` (201) | publica `TicketCreated` |
| POST | /api/tickets/{id}/messages | `AddMessageRequest(authorId, body)` | `TicketResponse` (200) | publica `TicketMessageAdded`; 404 se ticket não existe |
| POST | /api/tickets/{id}/close | — | `TicketResponse` (200) | idempotente |
| GET | /api/tickets?status= | — | `List<TicketResponse>` (200) | |

## Data Model
- `ticket.tickets`: `id (uuid, pk)`, `subject (varchar, not null)`, `status (varchar, not null)`, `requester_id (uuid, not null)`, `created_at (timestamp, not null)`, `closed_at (timestamp, nullable)`. Sem `assignee_id`: atribuicao de atendente e Non-Goal explicito (ver abaixo).
- `ticket.ticket_messages`: `id (uuid, pk)`, `ticket_id (uuid, not null, sem FK física — ver Riscos)`, `author_id (uuid, not null)`, `body (text, not null)`, `created_at (timestamp, not null)`.

## Events Published / Consumed
- Publishes: `TicketCreated(ticketId, subject, requesterId, createdAt)`, `TicketMessageAdded(ticketId, messageId, authorId, body, createdAt)`.
- Consumes: (nenhum)

## Acceptance Criteria
- [ ] Dado subject e requesterId válidos, quando `POST /api/tickets`, então cria o ticket e
      publica `TicketCreated`.
- [ ] Dado um ticket existente, quando `POST /api/tickets/{id}/messages`, então a mensagem é
      persistida e `TicketMessageAdded` é publicado.
- [ ] Dado um ticket já `CLOSED`, quando `POST /api/tickets/{id}/close` novamente, então retorna
      200 sem erro (idempotente).
- [ ] Dado tickets com status variados, quando `GET /api/tickets?status=OPEN`, então retorna só
      os `OPEN`.

## Riscos / Simplificações
- `ticket_messages.author_id` referencia `user.users.id` apenas logicamente — sem FK de banco
  entre módulos, para manter os módulos sem acoplamento de schema (tradeoff do monólito
  modular, documentado no README raiz).
- Sem outbox durável: a publicação de eventos usa o listener do Spring Modulith após commit,
  não um outbox persistido como em outros projetos do autor (ver README raiz).
