# Spec: knowledgebase

## Status
Approved

## Contexto
O assistente de IA precisa de material de apoio para sugerir respostas relevantes — artigos da
base de conhecimento (KB) são a fonte desse material. Este módulo cuida apenas de cadastro e
busca de artigos; a lógica de *como* esses artigos entram no prompt do modelo pertence ao
módulo `assistant` (ver `specs/assistant.md`), não a este.

## Goals
- Permitir cadastrar artigos de KB (título, corpo, tags).
- Permitir buscar artigos por palavras-chave, de forma relevante o suficiente para alimentar o
  `ContextBuilder` do módulo `assistant`.

## Non-Goals
- Busca semântica/vetorial (embeddings). A busca é por correspondência textual
  (`ILIKE`/`tsvector` do Postgres), não por similaridade semântica — ver Riscos.
- Edição, versionamento ou aprovação de artigos.
- Categorização hierárquica de tags (tags são uma lista simples de strings).

## Requirements
1. `POST /api/kb/articles` cria um artigo com `title`, `body`, `tags` e retorna 201.
2. `GET /api/kb/articles?q=<termo>` retorna artigos cujo título/corpo/tags combinam com o termo,
   ordenados por relevância textual (rank do Postgres `tsvector`), limitado a um top-N.
3. Sem `q`, `GET /api/kb/articles` lista todos os artigos (ordem de criação).

## API Contract

| Method | Path | Request | Response | Notes |
|---|---|---|---|---|
| POST | /api/kb/articles | `CreateArticleRequest(title, body, tags)` | `ArticleResponse` (201) | |
| GET | /api/kb/articles?q=termo | — | `List<ArticleResponse>` (200) | sem `q`, lista tudo |

## Data Model
- `knowledgebase.kb_articles`: `id (uuid, pk)`, `title (varchar, not null)`, `body (text, not null)`, `tags (varchar[], nullable)`, `created_at (timestamp, not null)`, `search_vector (tsvector, generated)`.

## Events Published / Consumed
- Publishes: (nenhum)
- Consumes: (nenhum)

## Acceptance Criteria
- [ ] Dado um artigo com título/corpo, quando `POST /api/kb/articles`, então retorna 201.
- [ ] Dado artigos existentes, quando `GET /api/kb/articles?q=impressora`, então retorna apenas
      os que mencionam "impressora" em título, corpo ou tags, ordenados por relevância.
- [ ] Dado nenhum artigo combina com `q`, então retorna lista vazia (200), não erro.

## Riscos / Simplificações
- Busca por keyword/`tsvector`, não embeddings — artigos com sinônimos não-literais
  (ex: "impressora" vs "equipamento de impressão") podem não ser encontrados. Documentado como
  simplificação deliberada; ver `specs/assistant.md` para como isso afeta o Context Engineering.
