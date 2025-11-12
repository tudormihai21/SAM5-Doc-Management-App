package com.example.docmanagement.Repositories;

import com.example.docmanagement.Domain.Team.Team;
import com.example.docmanagement.Domain.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Integer> {
    Optional<Team> findByManager(User manager);
}