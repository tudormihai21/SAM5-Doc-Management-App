package com.example.docmanagement.Repositories;

import com.example.docmanagement.Domain.Product.SoftwareProduct;
import com.example.docmanagement.Domain.Product.SoftwareRelease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SoftwareReleaseRepository extends JpaRepository<SoftwareRelease, Integer> {
    
    /**
     * Sprint 5: Fetch all releases with their products eagerly loaded.
     * This prevents LazyInitializationException when accessing product info
     * in Vaadin views after the transaction has closed.
     */
    @Query("SELECT sr FROM SoftwareRelease sr LEFT JOIN FETCH sr.product")
    List<SoftwareRelease> findAllWithProduct();

    /**
     * Find releases by product
     */
    @Query("SELECT sr FROM SoftwareRelease sr WHERE sr.product = :product")
    List<SoftwareRelease> findByProduct(@Param("product") SoftwareProduct product);

    /**
     * Find releases by product ID with product eagerly loaded
     */
    @Query("SELECT sr FROM SoftwareRelease sr LEFT JOIN FETCH sr.product WHERE sr.product.productId = :productId")
    List<SoftwareRelease> findByProductIdWithProduct(@Param("productId") int productId);
}
