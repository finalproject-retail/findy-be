CREATE TABLE IF NOT EXISTS chatbot_rag_documents (
    rag_document_id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    source_type VARCHAR(50) NOT NULL,
    source_name VARCHAR(255),
    content TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_chatbot_rag_documents_active
ON chatbot_rag_documents (is_active);

CREATE TABLE IF NOT EXISTS chatbot_rag_chunks (
    rag_chunk_id BIGSERIAL PRIMARY KEY,
    rag_document_id BIGINT NOT NULL,
    chunk_index INTEGER NOT NULL,
    content TEXT NOT NULL,
    token_count INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_chatbot_rag_chunks_document
        FOREIGN KEY (rag_document_id)
        REFERENCES chatbot_rag_documents (rag_document_id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_chatbot_rag_chunks_document_id
ON chatbot_rag_chunks (rag_document_id);

CREATE INDEX IF NOT EXISTS idx_chatbot_rag_chunks_active
ON chatbot_rag_chunks (is_active);

CREATE INDEX IF NOT EXISTS idx_chatbot_rag_chunks_content
ON chatbot_rag_chunks USING gin (to_tsvector('simple', content));