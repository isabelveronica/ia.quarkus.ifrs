package dev.langchain4j.quarkus.workshop;

import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import io.quarkus.logging.Log;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@ApplicationScoped
public class InMemoryEmbeddingStoreProvider {

    private static final String STORE_FILE_NAME = "embedding_store.json";

    @Produces
    @ApplicationScoped
    EmbeddingStore<TextSegment> embeddingStore() {
        Path path = Paths.get(System.getProperty("user.dir"), STORE_FILE_NAME);

        // Se o arquivo já existir no disco, carrega o estado anterior para evitar reindexação
        if (Files.exists(path)) {
            Log.info("Carregando banco de vetores existente a partir de: " + path.toAbsolutePath());
            return InMemoryEmbeddingStore.fromFile(path);
        }

        Log.info("Criando novo banco de vetores InMemory vazio.");
        InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        
        // Opcional: Garante que os dados sejam salvos ao fechar o app
        // Se você limpa o banco com `store.removeAll()` no StartupEvent, essa linha pode ser removida.
        // store.serializeToFile(path); 

        return store;
    }
}