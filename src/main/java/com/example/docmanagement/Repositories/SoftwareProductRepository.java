package com.example.docmanagement.Repositories;

import com.example.docmanagement.Domain.Product.SoftwareProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoftwareProductRepository extends JpaRepository<SoftwareProduct, Integer> {
}