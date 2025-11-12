package com.example.docmanagement.Repositories;

import com.example.docmanagement.Domain.Document.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // <-- IMPORT NOU
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List; // <-- IMPORT NOU
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Integer> {

    long countBySoftwareRelease_ReleaseId(int releaseId);
    @Query("SELECT d FROM Document d WHERE d.softwareRelease.product.productId = :productId")
    List<Document> findByProductId(int productId);


    @Query("SELECT d FROM Document d " +
            "LEFT JOIN FETCH d.uploader " +
            "LEFT JOIN FETCH d.softwareRelease " +
            "LEFT JOIN FETCH d.documentType")
    List<Document> findAllWithDetails();

    @Query("SELECT d FROM Document d " +
            "LEFT JOIN FETCH d.uploader " +
            "LEFT JOIN FETCH d.softwareRelease sr " +
            "LEFT JOIN FETCH d.documentType " +
            "WHERE sr.product.productId = :productId")
    List<Document> findByProductIdWithDetails(@Param("productId") int productId);
}