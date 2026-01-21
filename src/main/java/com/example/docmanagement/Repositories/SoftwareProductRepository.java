package com.example.docmanagement.Repositories;

import com.example.docmanagement.Domain.Product.SoftwareProduct;
import com.example.docmanagement.Domain.Team.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface SoftwareProductRepository extends JpaRepository<SoftwareProduct, Integer> {

    /**
     * Find all products with owner team eagerly loaded
     */
    @Query("SELECT p FROM SoftwareProduct p LEFT JOIN FETCH p.ownerTeam")
    List<SoftwareProduct> findAllWithOwnerTeam();

    /**
     * Sprint 5: Find products by owner team
     */
    @Query("SELECT p FROM SoftwareProduct p " +
            "LEFT JOIN FETCH p.ownerTeam " +
            "WHERE p.ownerTeam = :team")
    List<SoftwareProduct> findByOwnerTeam(@Param("team") Team team);

    /**
     * Sprint 5: Find products by multiple owner teams
     */
    @Query("SELECT p FROM SoftwareProduct p " +
            "LEFT JOIN FETCH p.ownerTeam " +
            "WHERE p.ownerTeam IN :teams")
    List<SoftwareProduct> findByOwnerTeamIn(@Param("teams") Set<Team> teams);

    /**
     * Sprint 5: Find products by team IDs
     */
    @Query("SELECT p FROM SoftwareProduct p " +
            "LEFT JOIN FETCH p.ownerTeam " +
            "WHERE p.ownerTeam.teamId IN :teamIds")
    List<SoftwareProduct> findByOwnerTeamIds(@Param("teamIds") Set<Integer> teamIds);
}