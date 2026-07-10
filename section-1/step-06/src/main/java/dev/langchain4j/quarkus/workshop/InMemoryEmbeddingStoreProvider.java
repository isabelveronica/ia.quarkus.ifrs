package dev.langchain4j.quarkus.workshop;

import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import dev.langchain4j.store.embedding.filter.Filter;
import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

@ApplicationScoped
public class InMemoryEmbeddingStoreProvider {

    @Produces
    @ApplicationScoped
    EmbeddingStore embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }
    // Exemplo: Filtrar dinamicamente para buscar APENAS no contexto financeiro/isenção
Filter filtroFinanceiro = metadataKey("categoria_filtro").isEqualTo("financeiro")
    .and(metadataKey("ano_semestre").isEqualTo("2026/2"));

}
