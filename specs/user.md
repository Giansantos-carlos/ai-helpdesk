# Spec: user

## Status
Approved

## Contexto
O sistema de helpdesk precisa identificar quem abre um ticket (requester) e quem o atende
(agent), para que o módulo `ticket` tenha a quem atribuir mensagens e o módulo `assistant` tenha
um perfil mínimo para dar tom à sugestão de resposta. Este módulo é deliberadamente o mais
simples do sistema — existe para dar suporte aos outros três, não como produto em si.

## Goals
- Permitir criar um usuário com papel (AGENT ou ADMIN) e nome/e-mail.
- Permitir buscar um usuário por id, para os outros módulos resolverem nomes/perfis.

## Non-Goals
- Autenticação real (login, senha, sessão, JWT). Os demais módulos identificam o usuário
  envolvido (requester, author) recebendo o `id` explicitamente no corpo da requisição — ver
  `specs/ticket.md` — sem verificação de identidade.
- Autorização/RBAC granular. O enum `UserRole` existe apenas para contexto, não é verificado em
  nenhum endpoint.
- Edição/remoção de usuário.

## Requirements
1. `POST /api/users` cria um usuário com `email`, `displayName`, `role` e retorna 201 com o
   recurso criado.
2. `email` é obrigatório e único; `displayName` é obrigatório.
3. `GET /api/users/{id}` retorna o usuário ou 404 (`ResourceNotFoundException`) se não existir.

## API Contract

| Method | Path | Request | Response | Notes |
|---|---|---|---|---|
| POST | /api/users | `CreateUserRequest(email, displayName, role)` | `UserResponse` (201) | 400 se e-mail duplicado ou campos ausentes |
| GET | /api/users/{id} | — | `UserResponse` (200) | 404 se não existir |

## Data Model
- `user.users`: `id (uuid, pk)`, `email (varchar, unique, not null)`, `display_name (varchar, not null)`, `role (varchar, not null)`, `created_at (timestamp, not null)`.

## Events Published / Consumed
- Publishes: (nenhum — usuário é consultado por outros módulos via API pública, não observado por evento)
- Consumes: (nenhum)

## Acceptance Criteria
- [ ] Dado um e-mail e nome válidos, quando `POST /api/users`, então retorna 201 com o id gerado.
- [ ] Dado um e-mail já cadastrado, quando `POST /api/users`, então retorna 400.
- [ ] Dado um id inexistente, quando `GET /api/users/{id}`, então retorna 404.

## Riscos / Simplificações
- Sem autenticação/senha: qualquer requisição pode se declarar como qualquer usuário via header.
  Aceitável para um projeto de demonstração focado nos 5 pilares de metodologia, não em
  segurança de identidade.
