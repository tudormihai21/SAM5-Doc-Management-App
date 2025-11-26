package com.example.docmanagement.Domain.Document;

import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Value Object representing audit information for documents.
 * This demonstrates the use of @Embeddable for value objects.
 */
@Embeddable
public class DocumentAuditInfo {

    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
    private String lastModifiedBy;
    private int versionCount;

    public DocumentAuditInfo() {
        this.createdAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
        this.versionCount = 1;
    }

    public DocumentAuditInfo(LocalDateTime createdAt, String lastModifiedBy) {
        this.createdAt = createdAt;
        this.lastModifiedAt = LocalDateTime.now();
        this.lastModifiedBy = lastModifiedBy;
        this.versionCount = 1;
    }

    public void incrementVersion(String modifiedBy) {
        this.versionCount++;
        this.lastModifiedAt = LocalDateTime.now();
        this.lastModifiedBy = modifiedBy;
    }

    // Getters
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastModifiedAt() {
        return lastModifiedAt;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public int getVersionCount() {
        return versionCount;
    }

    // Setters
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setLastModifiedAt(LocalDateTime lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public void setVersionCount(int versionCount) {
        this.versionCount = versionCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentAuditInfo that = (DocumentAuditInfo) o;
        return versionCount == that.versionCount &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(lastModifiedBy, that.lastModifiedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(createdAt, lastModifiedBy, versionCount);
    }
}