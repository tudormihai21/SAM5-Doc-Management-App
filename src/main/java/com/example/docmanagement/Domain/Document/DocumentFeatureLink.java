package com.example.docmanagement.Domain.Document;
import jakarta.persistence.*;
//IMPORT CLASA
import com.example.docmanagement.Domain.Product.Feature;

@Entity
@Table(name = "Document_Feature_Link")
public class DocumentFeatureLink {

    @EmbeddedId
    private DocumentFeatureLinkId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("documentId")
    @JoinColumn(name = "DocumentID")
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("featureId")
    @JoinColumn(name = "FeatureID")
    private Feature feature;

    // Constructori
    public DocumentFeatureLink() {
    }

    // Getters and Setters
    public DocumentFeatureLinkId getId() {
        return id;
    }

    public void setId(DocumentFeatureLinkId id) {
        this.id = id;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }
}
