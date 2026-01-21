package com.example.docmanagement.Repositories;

import com.example.docmanagement.Domain.Team.Team;
import com.example.docmanagement.Domain.Team.TeamMember;
import com.example.docmanagement.Domain.Team.TeamMemberId;
import com.example.docmanagement.Domain.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, TeamMemberId> {

    /**
     * Find all team memberships for a specific team
     */
    List<TeamMember> findByTeam(Team team);

    /**
     * Find the specific membership link between a user and a team
     */
    Optional<TeamMember> findByUserAndTeam(User user, Team team);

    /**
     * Sprint 5: Find all team memberships for a specific user WITH Team eagerly loaded
     * This prevents LazyInitializationException when accessing team properties
     */
    @Query("SELECT tm FROM TeamMember tm " +
            "LEFT JOIN FETCH tm.team " +
            "LEFT JOIN FETCH tm.user " +
            "WHERE tm.user = :user")
    List<TeamMember> findByUser(@Param("user") User user);

    /**
     * Sprint 5: Find all team memberships for a user by user ID with Team eagerly loaded
     */
    @Query("SELECT tm FROM TeamMember tm " +
            "LEFT JOIN FETCH tm.team " +
            "WHERE tm.user.userId = :userId")
    List<TeamMember> findByUserIdWithTeam(@Param("userId") int userId);

    /**
     * Sprint 5: Check if a user is a member of a specific team
     */
    boolean existsByUserAndTeam(User user, Team team);

    /**
     * Sprint 5: Count teams a user belongs to
     */
    @Query("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.user = :user")
    long countByUser(@Param("user") User user);

    /**
     * Sprint 5: Find team IDs for a user
     */
    @Query("SELECT tm.team.teamId FROM TeamMember tm WHERE tm.user.userId = :userId")
    List<Integer> findTeamIdsByUserId(@Param("userId") int userId);
}