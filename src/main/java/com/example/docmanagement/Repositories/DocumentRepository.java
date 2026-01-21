package com.example.docmanagement.Repositories;

import com.example.docmanagement.Domain.Document.Document;
import com.example.docmanagement.Domain.Team.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Integer> {

    long countBySoftwareRelease_ReleaseId(int releaseId);

    @Query("SELECT d FROM Document d WHERE d.softwareRelease.product.productId = :productId")
    List<Document> findByProductId(int productId);

    /**
     * Sprint 5: Fetch all documents with ALL related entities eagerly loaded
     * Including: uploader, softwareRelease, product (through release), ownerTeam, documentType
     * This prevents LazyInitializationException in Vaadin views
     */
    @Query("SELECT DISTINCT d FROM Document d " +
            "LEFT JOIN FETCH d.uploader u " +
            "LEFT JOIN FETCH u.role " +
            "LEFT JOIN FETCH d.softwareRelease sr " +
            "LEFT JOIN FETCH sr.product p " +
            "LEFT JOIN FETCH p.ownerTeam " +
            "LEFT JOIN FETCH d.documentType")
    List<Document> findAllWithDetails();

    /**
     * Find documents by product ID with all details
     */
    @Query("SELECT DISTINCT d FROM Document d " +
            "LEFT JOIN FETCH d.uploader u " +
            "LEFT JOIN FETCH u.role " +
            "LEFT JOIN FETCH d.softwareRelease sr " +
            "LEFT JOIN FETCH sr.product p " +
            "LEFT JOIN FETCH p.ownerTeam " +
            "LEFT JOIN FETCH d.documentType " +
            "WHERE p.productId = :productId")
    List<Document> findByProductIdWithDetails(@Param("productId") int productId);

    /**
     * Sprint 5: Find documents by team ownership
     * Returns documents where the product's owner team matches the given team
     */
    @Query("SELECT DISTINCT d FROM Document d " +
            "LEFT JOIN FETCH d.uploader u " +
            "LEFT JOIN FETCH u.role " +
            "LEFT JOIN FETCH d.softwareRelease sr " +
            "LEFT JOIN FETCH sr.product p " +
            "LEFT JOIN FETCH p.ownerTeam " +
            "LEFT JOIN FETCH d.documentType " +
            "WHERE p.ownerTeam = :team")
    List<Document> findByTeamWithDetails(@Param("team") Team team);

    /**
     * Sprint 5: Find documents by multiple teams
     * Returns documents where the product's owner team is in the given set of teams
     */
    @Query("SELECT DISTINCT d FROM Document d " +
            "LEFT JOIN FETCH d.uploader u " +
            "LEFT JOIN FETCH u.role " +
            "LEFT JOIN FETCH d.softwareRelease sr " +
            "LEFT JOIN FETCH sr.product p " +
            "LEFT JOIN FETCH p.ownerTeam " +
            "LEFT JOIN FETCH d.documentType " +
            "WHERE p.ownerTeam IN :teams")
    List<Document> findByTeamsWithDetails(@Param("teams") Set<Team> teams);

    /**
     * Sprint 5: Find documents by team IDs
     * Alternative method using team IDs instead of Team objects
     */
    @Query("SELECT DISTINCT d FROM Document d " +
            "LEFT JOIN FETCH d.uploader u " +
            "LEFT JOIN FETCH u.role " +
            "LEFT JOIN FETCH d.softwareRelease sr " +
            "LEFT JOIN FETCH sr.product p " +
            "LEFT JOIN FETCH p.ownerTeam " +
            "LEFT JOIN FETCH d.documentType " +
            "WHERE p.ownerTeam.teamId IN :teamIds")
    List<Document> findByTeamIdsWithDetails(@Param("teamIds") Set<Integer> teamIds);

    /**
     * Sprint 5: Find documents for a specific user based on their team memberships
     * This query joins through the user's team memberships to find relevant documents
     */
    @Query("SELECT DISTINCT d FROM Document d " +
            "LEFT JOIN FETCH d.uploader u " +
            "LEFT JOIN FETCH u.role " +
            "LEFT JOIN FETCH d.softwareRelease sr " +
            "LEFT JOIN FETCH sr.product p " +
            "LEFT JOIN FETCH p.ownerTeam " +
            "LEFT JOIN FETCH d.documentType " +
            "WHERE p.ownerTeam.teamId IN " +
            "(SELECT tm.team.teamId FROM TeamMember tm WHERE tm.user.userId = :userId)")
    List<Document> findByUserTeamMembershipWithDetails(@Param("userId") int userId);

    /**
     * Sprint 5: Count documents for a user based on their team memberships
     */
    @Query("SELECT COUNT(DISTINCT d) FROM Document d " +
            "WHERE d.softwareRelease.product.ownerTeam.teamId IN " +
            "(SELECT tm.team.teamId FROM TeamMember tm WHERE tm.user.userId = :userId)")
    long countByUserTeamMembership(@Param("userId") int userId);
}