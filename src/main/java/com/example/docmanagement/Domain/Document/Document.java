package com.example.docmanagement.Domain.Document;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import com.example.docmanagement.Domain.Product.SoftwareRelease;
import com.example.docmanagement.Domain.User.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


//Document Entity with JSON Serialization Annotations

@Entity
@Table(name = "Document")
//@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "featureLinks"})
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DocumentID")
    @JsonProperty("documentId")
    private int documentId;

    @Column(name = "Title")
    @JsonProperty("title")
    private String title;

    @Column(name = "FilePath")
    @JsonProperty("filePath")
    private String filePath;

    @Column(name = "DocumentVersion")
    @JsonProperty("version")
    private String documentVersion;

    @Column(name = "UploadTimestamp")
    @JsonProperty("uploadTimestamp")
    private LocalDateTime uploadTimestamp;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ReleaseID", referencedColumnName = "ReleaseID")
    @JsonProperty("release")
    @JsonIgnoreProperties({"documents", "features", "product"})
    private SoftwareRelease softwareRelease;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "TypeID", referencedColumnName = "TypeID")
    @JsonProperty("documentType")
    @JsonIgnoreProperties({"documents"})
    private DocumentType documentType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "UploaderID", referencedColumnName = "UserID")
    @JsonProperty("uploader")
    @JsonIgnoreProperties({"password", "uploadedDocuments", "teamMembers", "role"})
    private User uploader;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    @JsonProperty("status")
    private DocumentStatus status;

    @OneToMany(mappedBy = "document")
    @JsonIgnoreProperties("document")
    private Set<DocumentFeatureLink> featureLinks;

    @Embedded
    @JsonProperty("auditInfo")
    private DocumentAuditInfo auditInfo;

    public Document() {
    }

    // Getters and Setters
    public int getDocumentId() {
        return documentId;
    }

    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDocumentVersion() {
        return documentVersion;
    }

    public void setDocumentVersion(String documentVersion) {
        this.documentVersion = documentVersion;
    }

    public LocalDateTime getUploadTimestamp() {
        return uploadTimestamp;
    }

    public void setUploadTimestamp(LocalDateTime uploadTimestamp) {
        this.uploadTimestamp = uploadTimestamp;
    }

    public SoftwareRelease getSoftwareRelease() {
        return softwareRelease;
    }

    public void setSoftwareRelease(SoftwareRelease softwareRelease) {
        this.softwareRelease = softwareRelease;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public User getUploader() {
        return uploader;
    }

    public void setUploader(User uploader) {
        this.uploader = uploader;
    }

    public Set<DocumentFeatureLink> getFeatureLinks() {
        return featureLinks;
    }

    public void setFeatureLinks(Set<DocumentFeatureLink> featureLinks) {
        this.featureLinks = featureLinks;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public DocumentAuditInfo getAuditInfo() {
        return auditInfo;
    }

    public void setAuditInfo(DocumentAuditInfo auditInfo) {
        this.auditInfo = auditInfo;
    }
}
