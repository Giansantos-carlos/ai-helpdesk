CREATE TABLE kb_articles (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    tags VARCHAR(100)[] DEFAULT '{}',
    created_at TIMESTAMP NOT NULL
);
