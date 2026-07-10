package dev.langchain4j.quarkus.workshop;

import static dev.langchain4j.data.document.splitter.DocumentSplitters.recursive;

import java.io.File;
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
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;

@ApplicationScoped
public class RagIngestion {

    // O Apache Tika vai ler perfeitamente os .json e o .pdf que estão nas pastas
    private final DocumentParser multiformatParser = new ApacheTikaDocumentParser();

    /**
     * Varre a pasta raiz 'rag/', identifica as subpastas como contextos e faz a ingestão.
     */
    public void ingest(@Observes StartupEvent ev,
                       EmbeddingStore store, EmbeddingModel embeddingModel,
                       @ConfigProperty(name = "rag.location") Path rootDocumentsPath) {
        
        // Limpa o banco vetorial ao iniciar 
        store.removeAll(); 

        File rootDir = rootDocumentsPath.toFile();

        if (rootDir.exists() && rootDir.isDirectory()) {
            // Pega apenas os diretórios de dentro da pasta 'rag/'
            File[] subDirs = rootDir.listFiles(File::isDirectory);

            if (subDirs != null && subDirs.length > 0) {
                for (File subDir : subDirs) {
                    // O contexto terá o nome da pasta
                    String contextTag = subDir.getName(); 
                    ingestContext(store, embeddingModel, subDir.toPath(), contextTag);
                }
            } else {
                Log.warn("A pasta raiz 'rag' está vazia ou não contém subpastas de contexto.");
            }
        } else {
            Log.errorf("O caminho configurado em 'rag.location' não existe: %s", rootDocumentsPath);
        }
    }

    private void ingestContext(EmbeddingStore store, EmbeddingModel embeddingModel, Path path, String contextTag) {
        Log.infof("[RAG] Lendo pasta de contexto: '%s' (Caminho: %s)", contextTag, path);

        // Carrega recursivamente tudo o que está dentro da subpasta específica
        List<Document> list = FileSystemDocumentLoader.loadDocumentsRecursively(path, multiformatParser);

        if (list.isEmpty()) {
            Log.warnf("Nenhum documento encontrado na pasta '%s'", contextTag);
            return;
        }

        // Alimenta o metadado de cada documento com o nome da pasta correspondente
        list.forEach(doc -> {
            doc.metadata().put("contexto", contextTag);
            Log.infof("Documento '%s' associado ao contexto '%s'", doc.metadata().getString("file_name"), contextTag);
        });

        // Configura o fatiador e ingestor do LangChain4j
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .documentSplitter(recursive(100, 25, new HuggingFaceTokenCountEstimator()))
                .build();

        // Fatia os JSONs/PDFs, gera os vetores e salva no banco mantendo a tag de contexto
        ingestor.ingest(list);
        
        Log.infof("Contexto '%s' ingerido com sucesso! Total de arquivos: %d\n", contextTag, list.size());
    }
}