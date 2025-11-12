package com.example.docmanagement.Repositories;

import com.example.docmanagement.Domain.Team.Team;
import com.example.docmanagement.Domain.Team.TeamMember;
import com.example.docmanagement.Domain.Team.TeamMemberId;
import com.example.docmanagement.Domain.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, TeamMemberId> {

    // --- METODELE NOI DE CARE ARE NEVOIE TeamManagementView ---

    /**
     * Găsește toți membrii (legăturile) care aparțin de o anumită echipă.
     */
    List<TeamMember> findByTeam(Team team);

    /**
     * Găsește legătura specifică dintre UN user și O echipă.
     * Folosită pentru a șterge un membru.
     */
    Optional<TeamMember> findByUserAndTeam(User user, Team team);

    // Metodele 'save' și 'delete' sunt incluse automat de JpaRepository
}