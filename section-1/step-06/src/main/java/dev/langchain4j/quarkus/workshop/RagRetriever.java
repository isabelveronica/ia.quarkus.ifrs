// --8<-- [start:ragretriever-1]
package dev.langchain4j.quarkus.workshop;

import java.util.List;

import dev.langchain4j.data.message.ChatMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.injector.ContentInjector;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import io.quarkus.logging.Log;

public class RagRetriever {

    @Produces
    @ApplicationScoped
    public RetrievalAugmentor create(EmbeddingStore store, EmbeddingModel model) {
        
        var contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingModel(model)
                .embeddingStore(store)
                .maxResults(3)
                .build();

        return DefaultRetrievalAugmentor.builder()
                .contentRetriever(contentRetriever)
// --8<-- [end:ragretriever-1]
// --8<-- [start:ragretriever-3]
                .contentInjector(new ContentInjector() {
                    @Override
                    public UserMessage inject(List<Content> list, ChatMessage chatMessage) {
                        StringBuilder prompt = new StringBuilder();
                        
                        prompt.append("Você é o Guia Interativo Digital do IFRS Campus Porto Alegre.\n");
                        prompt.append("Sua função é responder dúvidas sobre o Processo Seletivo/Vestibular, suporte de TI e sobre as normas internas (ROD, calendários e portarias).\n\n");
                        
                        prompt.append("Diretrizes Estritas:\n");
                        prompt.append("0. SEJA CONCISO E DIRETO. Responda em no máximo 3 ou 4 frases curtas e objetivas. Evite explicações longas ou repetir o contexto.\n");
                        prompt.append("1. Baseie suas respostas APENAS nos fragmentos de documentos fornecidos no contexto abaixo.\n");
                        prompt.append("2. Se a informação sobre prazos, datas ou documentação específica de um curso não estiver no contexto, responda exatamente: \"Não encontrei essa informação nos documentos atuais do campus. Por favor, verifique diretamente no site oficial (poa.ifrs.edu.br) ou contate a Coordenadoria de Registros Acadêmicos (CRA).\"\n");
                        prompt.append("3. Sempre diferencie se a regra se aplica ao Ensino Técnico ou ao Ensino Superior ou a Pós Graduação.\n");
                        prompt.append("4. Ao final da resposta, cite explicitamente de qual documento, seção ou tags você extraiu a informação baseado nos metadados presentes no contexto.\n\n");
                        
                        prompt.append("--- CONTEXTO ATUAL DO CAMPUS ---\n");
                        list.forEach(content -> prompt.append(content.textSegment().text()).append("\n\n"));
                        prompt.append("--------------------------------\n\n");
                        
                        prompt.append("Pergunta do Usuário: ").append(((UserMessage) chatMessage).singleText());
                        
                        Log.info("Prompt final enviado ao LLM:\n" + prompt.toString());
                        return new UserMessage(prompt.toString());
                    }
                })
// --8<-- [end:ragretriever-3]
// --8<-- [start:ragretriever-2]
                .build();
    }
}
// --8<-- [end:ragretriever-2]