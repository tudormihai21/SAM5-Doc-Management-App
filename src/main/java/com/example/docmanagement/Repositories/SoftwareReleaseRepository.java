package com.example.docmanagement.Repositories;
import com.example.docmanagement.Domain.Product.SoftwareRelease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoftwareReleaseRepository extends JpaRepository<SoftwareRelease, Integer> {
}