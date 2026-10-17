## Spec

Link para o spec que motiva esta mudanca (ver `specs/`): <!-- specs/xxx.md -->

## O que mudou

<!-- Resumo objetivo das mudancas. -->

## Como foi testado

<!-- mvn verify localmente? teste manual via curl/docker-compose? -->

## Checklist

- [ ] `mvn verify` passa localmente (testes + `ModularityTests` + gate de cobertura)
- [ ] `mvn checkstyle:check` e `mvn compile spotbugs:check` passam localmente
- [ ] Nenhum segredo (API key, senha) foi commitado
- [ ] O spec em `specs/` foi atualizado, se o comportamento mudou
