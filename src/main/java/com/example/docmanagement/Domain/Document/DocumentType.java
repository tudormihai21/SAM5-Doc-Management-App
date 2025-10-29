package com.example.docmanagement.Domain.Document;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "DocumentType")
public class DocumentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TypeID")
    private int typeId;

    @Column(name = "TypeName")
    private String typeName;

    @OneToMany(mappedBy = "documentType")
    private Set<Document> documents;

    // Constructori
    public DocumentType() {
    }

    // Getters and Setters
    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Set<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(Set<Document> documents) {
        this.documents = documents;
    }
}
