package dev.langchain4j.quarkus.workshop;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

@Entity
public class RagRetrievalLog extends PanacheEntity {
    // O PanacheEntity já cria automaticamente um atributo 'id' do tipo Long que é AutoIncrement.

    public String userQuery;
    
    @Column(columnDefinition = "TEXT")
    public String recoveredChunk;
    
    public Double similarityScore;
    public String sourceDocument;
    public LocalDateTime timestamp;
}