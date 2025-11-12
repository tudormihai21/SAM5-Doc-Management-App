package com.example.docmanagement.Domain.Product;
import jakarta.persistence.*;
import java.util.Set;
// IMPORT CLASA
// CLASA cu DocumentFeatureLink
import com.example.docmanagement.Domain.Document.DocumentFeatureLink;

@Entity
@Table(name = "Feature")
public class Feature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FeatureID")
    private int featureId;

    @Column(name = "FeatureName")
    private String featureName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ReleaseID", referencedColumnName = "ReleaseID")
    private SoftwareRelease softwareRelease;

    @OneToMany(mappedBy = "feature")
    private Set<DocumentFeatureLink> documentLinks;


    public Feature() {
    }

    public int getFeatureId() {
        return featureId;
    }

    public void setFeatureId(int featureId) {
        this.featureId = featureId;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public SoftwareRelease getSoftwareRelease() {
        return softwareRelease;
    }

    public void setSoftwareRelease(SoftwareRelease softwareRelease) {
        this.softwareRelease = softwareRelease;
    }

    public Set<DocumentFeatureLink> getDocumentLinks() {
        return documentLinks;
    }

    public void setDocumentLinks(Set<DocumentFeatureLink> documentLinks) {
        this.documentLinks = documentLinks;
    }
}
