package dev.langchain4j.quarkus.workshop;

import java.util.List;
import java.util.Map;

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
import io.quarkus.logging.Log;

public class RagRetriever {

    @Produces
    @ApplicationScoped
    public RetrievalAugmentor create(EmbeddingStore store, EmbeddingModel model) {
        
       var contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingModel(model)
                .embeddingStore(store)
                .maxResults(6) 
                .build();

        return DefaultRetrievalAugmentor.builder()
                .contentRetriever(contentRetriever)
                .contentInjector(new ContentInjector() {
                    @Override
                    public UserMessage inject(List<Content> list, ChatMessage chatMessage) {
                        StringBuilder prompt = new StringBuilder();
                        
                        prompt.append("Você é o Guia Interativo Digital do IFRS Campus Porto Alegre.\n");
                        prompt.append("Sua função é responder dúvidas sobre o Processo Seletivo/Vestibular, suporte de TI e sobre as normas internas (ROD, calendários e portarias).\n\n");
                        
                        prompt.append("Diretrizes Estritas:\n");
                        prompt.append("0. SEJA CONCISO E DIRETO. Responda em no máximo 3 ou 4 frases curtas e objetivas.\n");
                        prompt.append("1. Baseie suas respostas estritamente no contexto fornecido abaixo. Use correspondência semântica e sinônimos (ex: Vestibular e Processo Seletivo são o mesmo contexto).\n");
                        prompt.append("2. Se a informação solicitada (como uma data específica não listada ou procedimento inexistente) realmente não puder ser deduzida ou encontrada no contexto, responda exatamente: \"Não encontrei essa informação nos documentos atuais do campus. Por favor, verifique diretamente no site oficial (poa.ifrs.edu.br) ou contate a Coordenadoria de Registros Acadêmicos (CRA).\"\n");
                        prompt.append("3. Sempre diferencie se a regra se aplica ao Ensino Técnico, Ensino Superior ou Pós-Graduação, se essa distinção estiver clara.\n");
                        prompt.append("4. Ao final da resposta, em uma linha separada, cite a fonte e tags baseando-se nos metadados anexados a cada fragmento.\n\n");
                        
                        prompt.append("--- CONTEXTO ATUAL DO CAMPUS (TEXTO + METADADOS) ---\n");
                        
                        
                    list.forEach(content -> {
                        Map<String, Object> metadata = content.textSegment().metadata().toMap();
                        
                        String contextoTag = String.valueOf(metadata.getOrDefault("contexto", "Geral"));
                        String assuntoTag = String.valueOf(metadata.getOrDefault("assunto", "N/A"));
                        String publicoTag = String.valueOf(metadata.getOrDefault("publico", "Geral"));

                        prompt.append("[Origem/Contexto: ").append(contextoTag)
                            .append(" | Assunto: ").append(assuntoTag)
                            .append(" | Público: ").append(publicoTag).append("]\n");
                            
                        prompt.append("Texto: ").append(content.textSegment().text()).append("\n\n");
                    });
                        
                        prompt.append("--------------------------------\n\n");
                        
                        prompt.append("Pergunta do Usuário: ").append(((UserMessage) chatMessage).singleText());
                        
                        Log.info("Prompt final enviado ao LLM:\n" + prompt.toString());
                        return new UserMessage(prompt.toString());
                    }
                })
                .build();
    }
}