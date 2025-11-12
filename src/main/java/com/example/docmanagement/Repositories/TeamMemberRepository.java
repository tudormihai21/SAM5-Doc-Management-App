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


    /**
     * Membrii care aparțin de o anumita echipa.
     */
    List<TeamMember> findByTeam(Team team);

    /**
     * Gaseste legatura specifica dintre un user si o echipa.

     */
    Optional<TeamMember> findByUserAndTeam(User user, Team team);

}