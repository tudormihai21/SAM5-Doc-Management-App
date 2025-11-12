package com.example.docmanagement.Domain.Document;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
// IMPORT CLASE
import com.example.docmanagement.Domain.Product.SoftwareRelease;
import com.example.docmanagement.Domain.User.User;

@Entity
@Table(name = "Document")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DocumentID")
    private int documentId;

    @Column(name = "Title")
    private String title;

    @Column(name = "FilePath")
    private String filePath;

    @Column(name = "DocumentVersion")
    private String documentVersion;

    @Column(name = "UploadTimestamp")
    private LocalDateTime uploadTimestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ReleaseID", referencedColumnName = "ReleaseID")
    private SoftwareRelease softwareRelease;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TypeID", referencedColumnName = "TypeID")
    private DocumentType documentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UploaderID", referencedColumnName = "UserID")
    private User uploader;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private DocumentStatus status;

    @OneToMany(mappedBy = "document")
    private Set<DocumentFeatureLink> featureLinks;


    public Document() {
    }

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
}
