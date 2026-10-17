-- Tabela de infraestrutura do Spring Modulith (spring-modulith-starter-jpa), nao de nenhum
-- modulo de negocio: registra a publicacao/conclusao de eventos de dominio entre modulos
-- (ex: TicketCreated -> listener do assistant). Schema conforme o esperado pela entidade
-- JpaEventPublication; ddl-auto=validate exige que ela exista via migration.
CREATE TABLE event_publication (
    id UUID PRIMARY KEY,
    listener_id VARCHAR(512) NOT NULL,
    event_type VARCHAR(512) NOT NULL,
    serialized_event VARCHAR(4000) NOT NULL,
    publication_date TIMESTAMP NOT NULL,
    completion_date TIMESTAMP
);
