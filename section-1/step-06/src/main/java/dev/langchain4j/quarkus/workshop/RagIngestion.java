package dev.langchain4j.quarkus.workshop;

import static dev.langchain4j.data.document.splitter.DocumentSplitters.recursive;

import java.nio.file.Path;
import java.util.List;

import dev.langchain4j.model.embedding.onnx.HuggingFaceTokenCountEstimator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser; // Importado
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;

@ApplicationScoped
public class RagIngestion {

    // Instancia o parser do Apache Tika, que extrai textos de .pdf, .docx e .txt nativamente
    private final DocumentParser multiformatParser = new ApacheTikaDocumentParser();

    /**
     * Coordena a ingestão de múltiplos contextos no banco de embeddings ao iniciar a aplicação.
     */
    public void ingest(@Observes StartupEvent ev,
                       EmbeddingStore store, EmbeddingModel embeddingModel,
                       @ConfigProperty(name = "rag.location") Path documents) {
        
        // Limpa o banco para começar do zero (apenas para fins de demonstração/desenvolvimento)
        store.removeAll(); 

        // 1. Ingestão do contexto geral
        ingestContext(store, embeddingModel, documents, "geral");
    }

    private void ingestContext(EmbeddingStore store, EmbeddingModel embeddingModel, Path path, String contextTag) {
        Log.infof("Iniciando carregamento multiformato (.txt, .docx, .pdf) do caminho: %s com a tag de contexto: '%s'", path, contextTag);

        // Agora o Loader sabe como ler PDF e Word dentro da pasta de forma recursiva.
        List<Document> list = FileSystemDocumentLoader.loadDocumentsRecursively(path, multiformatParser);

        // Injeta o metadado que diferencia este contexto dos outros antes do split
        list.forEach(doc -> doc.metadata().put("contexto", contextTag));

        // Configura o pipeline do LangChain4j para este lote de documentos
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .documentSplitter(recursive(100, 25, new HuggingFaceTokenCountEstimator()))
                .build();

        // Divide o texto, gera os vetores e salva no banco mantendo as tags nos metadados de cada bloco (TextSegment)
        ingestor.ingest(list);
        
        Log.infof("Contexto '%s' ingerido com sucesso no banco vetorial. Total de arquivos processados: %d", contextTag, list.size());
    }
}