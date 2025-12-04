package com.example.docmanagement.Repositories;

import com.example.docmanagement.Domain.Product.SoftwareProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SoftwareProductRepository extends JpaRepository<SoftwareProduct, Integer> {
    @Query("SELECT p FROM SoftwareProduct p LEFT JOIN FETCH p.ownerTeam")
    List<SoftwareProduct> findAllWithOwnerTeam();
}