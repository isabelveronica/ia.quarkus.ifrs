package dev.langchain4j.quarkus.workshop;

import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class InMemoryEmbeddingStoreProvider {

    @Produces
    @ApplicationScoped
    EmbeddingStore embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }
}