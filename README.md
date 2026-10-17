# AI Helpdesk

Projeto de referencia: um sistema de helpdesk/suporte com um assistente de IA que sugere
respostas a atendentes, construido para demonstrar concretamente 5 pilares de metodologia de
desenvolvimento. Nao e um produto completo — e uma fatia vertical fina por modulo, pensada para
ser lida e entendida, nao para ir para producao como esta.

## Os 5 pilares e onde encontra-los

| Pilar | Artefato concreto |
|---|---|
| **Spec Driven Development** | [`specs/TEMPLATE.md`](specs/TEMPLATE.md) + um spec por modulo (`specs/user.md`, `specs/knowledgebase.md`, `specs/ticket.md`, `specs/assistant.md`), comitados **antes** do codigo de cada modulo — confira `git log --follow specs/` vs. o primeiro commit de `src/main/java/.../<modulo>/`. |
| **Arquitetura Monolitica Modular** | Um unico artefato Maven (`pom.xml`), pacotes `com.giansantos.helpdesk.{user,knowledgebase,ticket,assistant,shared}` com `internal/` (privado) + `api/`/`event/` (publico, marcado com `@NamedInterface`). Limites verificados automaticamente por [`ModularityTests`](src/test/java/com/giansantos/helpdesk/ModularityTests.java) (`ApplicationModules.verify()` via Spring Modulith). |
| **Context Engineering** | [`ContextBuilder`](src/main/java/com/giansantos/helpdesk/assistant/internal/service/ContextBuilder.java) — rankeia historico de ticket + artigos de KB em tempo real e trunca contra um orcamento de caracteres, descartando primeiro os itens de menor score. Testado em [`ContextBuilderTest`](src/test/java/com/giansantos/helpdesk/assistant/ContextBuilderTest.java). |
| **Code Review** | [`.github/workflows/code-review.yml`](.github/workflows/code-review.yml) (Checkstyle + SpotBugs + Semgrep), [`CODEOWNERS`](CODEOWNERS), [`.github/PULL_REQUEST_TEMPLATE.md`](.github/PULL_REQUEST_TEMPLATE.md). |
| **Quality Gate** | [`.github/workflows/ci.yml`](.github/workflows/ci.yml) (`mvn verify`: testes + `ModularityTests` + gate de cobertura JaCoCo) → [`.github/workflows/deploy.yml`](.github/workflows/deploy.yml) (so dispara se `ci.yml` passar em `main`). |

## Dominio

Helpdesk com 4 modulos:

- **user** — agentes/admins (sem autenticacao real, ver Riscos).
- **knowledgebase** — artigos com busca por palavra-chave (Postgres `tsvector`).
- **ticket** — tickets e mensagens; publica `TicketCreated`/`TicketMessageAdded`.
- **assistant** — reage a `TicketCreated` de forma assincrona, monta contexto (`ContextBuilder`),
  chama a API da Anthropic e persiste uma sugestao de resposta.

Comunicacao entre modulos e sempre via evento Spring (`ticket` → `assistant`) ou via uma
interface de consulta publica (`UserQueryApi`, `KnowledgeBaseQueryApi`, `TicketQueryApi`) —
nunca acessando `internal` de outro modulo.

## Rodando localmente

```bash
docker compose up -d          # Postgres
export ANTHROPIC_API_KEY=...  # opcional: sem isso, sugestoes ficam FAILED (degradacao graciosa)
mvn spring-boot:run
```

Fluxo de ponta a ponta:

```bash
# 1. criar usuario
curl -s -X POST localhost:8080/api/users -H 'Content-Type: application/json' \
  -d '{"email":"a@b.com","displayName":"Ana","role":"AGENT"}'

# 2. criar ticket (use o id retornado acima como requesterId)
curl -s -X POST localhost:8080/api/tickets -H 'Content-Type: application/json' \
  -d '{"subject":"Impressora nao liga","requesterId":"<uuid-do-usuario>"}'

# 3. apos alguns segundos (processamento assincrono), consultar a sugestao
curl -s localhost:8080/api/tickets/<uuid-do-ticket>/suggestion
```

## Riscos / Simplificacoes (deliberadas, nao esquecidas)

- **Sem autenticacao real**: ids de usuario sao passados explicitamente no corpo das
  requisicoes, sem verificacao de identidade.
- **Busca de KB por keyword/`tsvector`, nao embeddings**: sinonimos nao-literais podem nao ser
  encontrados. Orcamento de contexto e uma heuristica de caracteres, nao tokenizacao real.
- **Sem FK fisica entre tabelas de modulos diferentes**: integridade referencial entre modulos
  e responsabilidade da aplicacao, nao do banco — tradeoff explicito do monolito modular.
- **Sem outbox duravel**: o listener do `assistant` (`@ApplicationModuleListener`) roda apos o
  commit, mas nao e reprocessado se o processo cair antes de terminar. Contraste deliberado com
  o padrao de outbox usado no projeto irmao `event-order-system`.
- **Deploy simulado**: `deploy.yml` apenas builda o jar e imprime um log; nao ha infraestrutura
  de destino real.
- **Protecao de branch (exigir aprovacao do CODEOWNERS + status checks de CI) e uma
  configuracao do GitHub**, nao algo que o YAML force por si so — precisa ser habilitada
  manualmente nas settings do repositorio.
- **Limite de cobertura JaCoCo (60%) e arbitrario para uma demonstracao**, nao um numero
  calibrado para este dominio.
- **`CODEOWNERS` aponta para `@Giansantos-carlos`** — ajuste para o(s) revisor(es) reais antes
  de usar este repositorio como template.
- **Teste de integracao via Testcontainers (`TicketCreationIntegrationTest`) pode falhar em
  ambientes locais com Docker Engine muito recente** (incompatibilidade conhecida entre
  `docker-java`/Testcontainers e versoes de API do Docker acima de ~1.44 neste momento); roda
  normalmente em runners padrao do GitHub Actions.
