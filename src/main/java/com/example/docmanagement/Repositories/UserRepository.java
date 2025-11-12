package com.example.docmanagement.Repositories;

import com.example.docmanagement.Domain.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    List<User> findByRole_RoleName(String roleName);
    @Query("SELECT u FROM User u WHERE u.role.roleName = 'TEAM_MEMBER' AND u.teamMembers IS EMPTY")
    List<User> findAvailableTeamMembers();
}
