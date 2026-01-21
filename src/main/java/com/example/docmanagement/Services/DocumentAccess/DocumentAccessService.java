package com.example.docmanagement.Services.DocumentAccess;

import com.example.docmanagement.Domain.Document.Document;
import com.example.docmanagement.Domain.Product.SoftwareProduct;
import com.example.docmanagement.Domain.Team.Team;
import com.example.docmanagement.Domain.Team.TeamMember;
import com.example.docmanagement.Domain.User.User;
import com.example.docmanagement.Repositories.DocumentRepository;
import com.example.docmanagement.Repositories.SoftwareProductRepository;
import com.example.docmanagement.Repositories.TeamMemberRepository;
import com.example.docmanagement.Services.Security.SecurityService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sprint 5: Document Access Service
 *
 * Centralizes the logic for determining which documents a user can access
 * based on their role and team membership.
 *
 * Access Rules:
 * - ADMIN: Can see ALL documents
 * - PROJECT_MANAGER: Can see ALL documents (they manage across teams)
 * - TEAM_MEMBER: Can only see documents from products owned by their team(s)
 */
@Service
public class DocumentAccessService {

    private final DocumentRepository documentRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final SoftwareProductRepository productRepository;
    private final SecurityService securityService;

    public DocumentAccessService(
            DocumentRepository documentRepository,
            TeamMemberRepository teamMemberRepository,
            SoftwareProductRepository productRepository,
            SecurityService securityService) {
        this.documentRepository = documentRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.productRepository = productRepository;
        this.securityService = securityService;
    }

    /**
     * Get all documents accessible to the current user
     */
    public List<Document> getAccessibleDocuments() {
        Optional<User> currentUser = SecurityService.getAuthenticatedUser();
        if (currentUser.isEmpty()) {
            return Collections.emptyList();
        }
        return getAccessibleDocuments(currentUser.get());
    }

    /**
     * Get all documents accessible to a specific user
     */
    public List<Document> getAccessibleDocuments(User user) {
        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "";

        // Admins and Project Managers can see all documents
        if ("ADMIN".equals(roleName) || "PROJECT_MANAGER".equals(roleName)) {
            return documentRepository.findAllWithDetails();
        }

        // Team Members can only see documents from their team's products
        if ("TEAM_MEMBER".equals(roleName)) {
            return getDocumentsForUserTeams(user);
        }

        // Default: no access
        return Collections.emptyList();
    }

    /**
     * Get documents for products owned by the user's teams
     */
    public List<Document> getDocumentsForUserTeams(User user) {
        Set<Team> userTeams = getUserTeams(user);

        if (userTeams.isEmpty()) {
            // User is not part of any team - return empty list
            return Collections.emptyList();
        }

        return documentRepository.findByTeamsWithDetails(userTeams);
    }

    /**
     * Get all teams a user belongs to (with Team eagerly loaded)
     */
    public Set<Team> getUserTeams(User user) {
        // Use the repository method that eagerly fetches Team
        return teamMemberRepository.findByUser(user).stream()
                .map(TeamMember::getTeam)
                .collect(Collectors.toSet());
    }

    /**
     * Get team IDs for a user
     */
    public Set<Integer> getUserTeamIds(User user) {
        return getUserTeams(user).stream()
                .map(Team::getTeamId)
                .collect(Collectors.toSet());
    }

    /**
     * Check if user can access a specific document
     */
    public boolean canAccessDocument(User user, Document document) {
        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "";

        // Admins and Project Managers can access all documents
        if ("ADMIN".equals(roleName) || "PROJECT_MANAGER".equals(roleName)) {
            return true;
        }

        // For Team Members, check if the document's product belongs to one of their teams
        if ("TEAM_MEMBER".equals(roleName)) {
            Set<Integer> userTeamIds = getUserTeamIds(user);

            if (document.getSoftwareRelease() != null
                    && document.getSoftwareRelease().getProduct() != null
                    && document.getSoftwareRelease().getProduct().getOwnerTeam() != null) {
                int documentTeamId = document.getSoftwareRelease().getProduct().getOwnerTeam().getTeamId();
                return userTeamIds.contains(documentTeamId);
            }
        }

        return false;
    }

    /**
     * Check if current user can access a specific document
     */
    public boolean canCurrentUserAccessDocument(Document document) {
        Optional<User> currentUser = SecurityService.getAuthenticatedUser();
        return currentUser.map(user -> canAccessDocument(user, document)).orElse(false);
    }

    /**
     * Get products accessible to the current user
     */
    public List<SoftwareProduct> getAccessibleProducts() {
        Optional<User> currentUser = SecurityService.getAuthenticatedUser();
        if (currentUser.isEmpty()) {
            return Collections.emptyList();
        }
        return getAccessibleProducts(currentUser.get());
    }

    /**
     * Get products accessible to a specific user
     */
    public List<SoftwareProduct> getAccessibleProducts(User user) {
        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "";

        // Admins and Project Managers can see all products
        if ("ADMIN".equals(roleName) || "PROJECT_MANAGER".equals(roleName)) {
            return productRepository.findAllWithOwnerTeam();
        }

        // Team Members can only see products owned by their teams
        if ("TEAM_MEMBER".equals(roleName)) {
            Set<Team> userTeams = getUserTeams(user);
            if (userTeams.isEmpty()) {
                return Collections.emptyList();
            }

            // Use the repository method to query directly (more efficient)
            return productRepository.findByOwnerTeamIn(userTeams);
        }

        return Collections.emptyList();
    }

    /**
     * Check if user is Admin or Project Manager (has full access)
     */
    public boolean hasFullAccess(User user) {
        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "";
        return "ADMIN".equals(roleName) || "PROJECT_MANAGER".equals(roleName);
    }

    /**
     * Check if current user has full access
     */
    public boolean currentUserHasFullAccess() {
        Optional<User> currentUser = SecurityService.getAuthenticatedUser();
        return currentUser.map(this::hasFullAccess).orElse(false);
    }
}