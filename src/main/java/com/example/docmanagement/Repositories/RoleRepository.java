package com.example.docmanagement.Repositories;

import com.example.docmanagement.Domain.User.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    // Spring Data JPA va genera automat totul
}