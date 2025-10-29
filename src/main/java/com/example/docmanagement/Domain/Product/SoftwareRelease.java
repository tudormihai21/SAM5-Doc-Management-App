package com.example.docmanagement.Domain.Product;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Set;
// IMPORT CLASA
// Clasa de documente
import com.example.docmanagement.Domain.Document.Document;
@Entity
@Table(name = "SoftwareRelease")
public class SoftwareRelease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReleaseID")
    private int releaseId;

    @Column(name = "VersionNumber")
    private String versionNumber;

    @Column(name = "ReleaseDate")
    private LocalDate releaseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductID", referencedColumnName = "ProductID")
    private SoftwareProduct product;

    @OneToMany(mappedBy = "softwareRelease")
    private Set<Feature> features;

    @OneToMany(mappedBy = "softwareRelease")
    private Set<Document> documents;

    // Constructori
    public SoftwareRelease() {
    }

    // Getters and Setters
    public int getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(int releaseId) {
        this.releaseId = releaseId;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public SoftwareProduct getProduct() {
        return product;
    }

    public void setProduct(SoftwareProduct product) {
        this.product = product;
    }

    public Set<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(Set<Feature> features) {
        this.features = features;
    }

    public Set<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(Set<Document> documents) {
        this.documents = documents;
    }
}
