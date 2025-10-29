package com.example.docmanagement.Repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
//IMPORT CLASA
import com.example.docmanagement.Domain.Document.Document;
@Repository
public interface DocumentRepository extends JpaRepository<Document, Integer> {

    long countBySoftwareRelease_ReleaseId(int releaseId);
}
