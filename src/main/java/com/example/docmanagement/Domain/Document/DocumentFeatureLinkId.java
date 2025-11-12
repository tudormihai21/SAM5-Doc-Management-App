package com.example.docmanagement.Domain.Document;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DocumentFeatureLinkId implements Serializable {

    @Column(name = "DocumentID")
    private int documentId;

    @Column(name = "FeatureID")
    private int featureId;


    public DocumentFeatureLinkId() {
    }

    public DocumentFeatureLinkId(int documentId, int featureId) {
        this.documentId = documentId;
        this.featureId = featureId;
    }


    public int getDocumentId() {
        return documentId;
    }

    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }

    public int getFeatureId() {
        return featureId;
    }

    public void setFeatureId(int featureId) {
        this.featureId = featureId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentFeatureLinkId that = (DocumentFeatureLinkId) o;
        return documentId == that.documentId && featureId == that.featureId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentId, featureId);
    }
}
