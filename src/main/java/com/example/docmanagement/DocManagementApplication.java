package com.example.docmanagement;

import com.example.docmanagement.Repositories.*;
// Import all required entities
import com.example.docmanagement.Domain.Document.*;
import com.example.docmanagement.Domain.Product.*;
import com.example.docmanagement.Domain.Team.*;
import com.example.docmanagement.Domain.User.*;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootApplication
public class DocManagementApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DocManagementApplication.class);
        app.setWebApplicationType(WebApplicationType.SERVLET);
        app.run(args);
    }

    @Bean
    public CommandLineRunner loadTestData(
            RoleRepository roleRepository,
            UserRepository userRepository,
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository,  // Added for team membership
            SoftwareProductRepository productRepository,
            SoftwareReleaseRepository releaseRepository,
            DocumentTypeRepository typeRepository,
            DocumentRepository documentRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }

            // ============================================
            // 1. CREATE ROLES
            // ============================================
            Role adminRole = new Role();
            adminRole.setRoleName("ADMIN");
            adminRole = roleRepository.save(adminRole);

            Role managerRole = new Role();
            managerRole.setRoleName("PROJECT_MANAGER");
            managerRole = roleRepository.save(managerRole);

            Role memberRole = new Role();
            memberRole.setRoleName("TEAM_MEMBER");
            memberRole = roleRepository.save(memberRole);

            // ============================================
            // 2. CREATE USERS
            // ============================================
            User adminUser = new User();
            adminUser.setFirstName("Admin");
            adminUser.setLastName("Site");
            adminUser.setEmail("admin@doc.com");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setRole(adminRole);
            adminUser = userRepository.save(adminUser);

            User managerUser = new User();
            managerUser.setFirstName("Ana");
            managerUser.setLastName("Popescu");
            managerUser.setEmail("ana.popescu@doc.com");
            managerUser.setPassword(passwordEncoder.encode("manager123"));
            managerUser.setRole(managerRole);
            managerUser = userRepository.save(managerUser);

            User devUser = new User();
            devUser.setFirstName("Ion");
            devUser.setLastName("Vasilescu");
            devUser.setEmail("ion.vasilescu@doc.com");
            devUser.setPassword(passwordEncoder.encode("dev123"));
            devUser.setRole(memberRole);
            devUser = userRepository.save(devUser);

            // Create a second team member for testing (not in any team initially)
            User devUser2 = new User();
            devUser2.setFirstName("Maria");
            devUser2.setLastName("Ionescu");
            devUser2.setEmail("maria.ionescu@doc.com");
            devUser2.setPassword(passwordEncoder.encode("dev123"));
            devUser2.setRole(memberRole);
            devUser2 = userRepository.save(devUser2);

            // Create a third team member for Beta team
            User devUser3 = new User();
            devUser3.setFirstName("Andrei");
            devUser3.setLastName("Dumitru");
            devUser3.setEmail("andrei.dumitru@doc.com");
            devUser3.setPassword(passwordEncoder.encode("dev123"));
            devUser3.setRole(memberRole);
            devUser3 = userRepository.save(devUser3);

            // ============================================
            // 3. CREATE TEAMS
            // ============================================
            Team teamAlpha = new Team();
            teamAlpha.setTeamName("Echipa Alpha");
            teamAlpha.setManager(managerUser);
            teamAlpha = teamRepository.save(teamAlpha);

            Team teamBeta = new Team();
            teamBeta.setTeamName("Echipa Beta");
            teamBeta.setManager(managerUser);
            teamBeta = teamRepository.save(teamBeta);

            // ============================================
            // 4. ADD USERS TO TEAMS (TeamMember relationships)
            // ============================================

            // Add Ion Vasilescu to Team Alpha
            TeamMember membership1 = new TeamMember();
            TeamMemberId membershipId1 = new TeamMemberId(devUser.getUserId(), teamAlpha.getTeamId());
            membership1.setId(membershipId1);
            membership1.setUser(devUser);
            membership1.setTeam(teamAlpha);
            teamMemberRepository.save(membership1);

            // Add Andrei Dumitru to Team Beta
            TeamMember membership2 = new TeamMember();
            TeamMemberId membershipId2 = new TeamMemberId(devUser3.getUserId(), teamBeta.getTeamId());
            membership2.setId(membershipId2);
            membership2.setUser(devUser3);
            membership2.setTeam(teamBeta);
            teamMemberRepository.save(membership2);

            // NOTE: Maria Ionescu is NOT added to any team
            // This allows testing the "no team assigned" scenario

            // ============================================
            // 5. CREATE PRODUCTS (with team ownership)
            // ============================================
            SoftwareProduct productAlpha = new SoftwareProduct();
            productAlpha.setProductName("DocManagement V1");
            productAlpha.setOwnerTeam(teamAlpha);  // Owned by Team Alpha
            productAlpha = productRepository.save(productAlpha);

            SoftwareProduct productBeta = new SoftwareProduct();
            productBeta.setProductName("ReportGenerator Pro");
            productBeta.setOwnerTeam(teamBeta);  // Owned by Team Beta
            productBeta = productRepository.save(productBeta);

            // Product without team (edge case testing)
            SoftwareProduct productNoTeam = new SoftwareProduct();
            productNoTeam.setProductName("Legacy System");
            productNoTeam.setOwnerTeam(null);  // No team owner
            productNoTeam = productRepository.save(productNoTeam);

            // ============================================
            // 6. CREATE RELEASES
            // ============================================
            SoftwareRelease releaseAlpha1 = new SoftwareRelease();
            releaseAlpha1.setVersionNumber("1.0.0-BETA");
            releaseAlpha1.setReleaseDate(LocalDate.now());
            releaseAlpha1.setProduct(productAlpha);
            releaseAlpha1 = releaseRepository.save(releaseAlpha1);

            SoftwareRelease releaseAlpha2 = new SoftwareRelease();
            releaseAlpha2.setVersionNumber("1.1.0");
            releaseAlpha2.setReleaseDate(LocalDate.now().plusDays(30));
            releaseAlpha2.setProduct(productAlpha);
            releaseAlpha2 = releaseRepository.save(releaseAlpha2);

            SoftwareRelease releaseBeta1 = new SoftwareRelease();
            releaseBeta1.setVersionNumber("2.0.0");
            releaseBeta1.setReleaseDate(LocalDate.now());
            releaseBeta1.setProduct(productBeta);
            releaseBeta1 = releaseRepository.save(releaseBeta1);

            SoftwareRelease releaseNoTeam = new SoftwareRelease();
            releaseNoTeam.setVersionNumber("0.9.0-LEGACY");
            releaseNoTeam.setReleaseDate(LocalDate.now().minusYears(1));
            releaseNoTeam.setProduct(productNoTeam);
            releaseNoTeam = releaseRepository.save(releaseNoTeam);

            // ============================================
            // 7. CREATE DOCUMENT TYPES
            // ============================================
            DocumentType specType = new DocumentType();
            specType.setTypeName("Specificatii Tehnice");
            specType = typeRepository.save(specType);

            DocumentType userManualType = new DocumentType();
            userManualType.setTypeName("Manual Utilizator");
            userManualType = typeRepository.save(userManualType);

            DocumentType apiDocType = new DocumentType();
            apiDocType.setTypeName("Documentatie API");
            apiDocType = typeRepository.save(apiDocType);

            // ============================================
            // 8. CREATE TEST DOCUMENTS
            // ============================================

            // Documents for Team Alpha's product
            Document doc1 = new Document();
            doc1.setTitle("Specificatii API Login");
            doc1.setFilePath("/docs/api_login_v1.pdf");
            doc1.setDocumentVersion("v1.0");
            doc1.setUploadTimestamp(LocalDateTime.now());
            doc1.setSoftwareRelease(releaseAlpha1);
            doc1.setDocumentType(specType);
            doc1.setUploader(devUser);
            doc1.setStatus(DocumentStatus.PENDING_REVIEW);
            documentRepository.save(doc1);

            Document doc2 = new Document();
            doc2.setTitle("Manual Utilizator DocManagement");
            doc2.setFilePath("/docs/user_manual_v1.pdf");
            doc2.setDocumentVersion("v1.0");
            doc2.setUploadTimestamp(LocalDateTime.now().minusDays(5));
            doc2.setSoftwareRelease(releaseAlpha1);
            doc2.setDocumentType(userManualType);
            doc2.setUploader(devUser);
            doc2.setStatus(DocumentStatus.APPROVED);
            documentRepository.save(doc2);

            Document doc3 = new Document();
            doc3.setTitle("Specificatii Modul Rapoarte");
            doc3.setFilePath("/docs/reports_spec_v1.1.pdf");
            doc3.setDocumentVersion("v1.1");
            doc3.setUploadTimestamp(LocalDateTime.now().minusDays(2));
            doc3.setSoftwareRelease(releaseAlpha2);
            doc3.setDocumentType(specType);
            doc3.setUploader(devUser);
            doc3.setStatus(DocumentStatus.DRAFT);
            documentRepository.save(doc3);

            // Documents for Team Beta's product
            Document doc4 = new Document();
            doc4.setTitle("ReportGenerator API Documentation");
            doc4.setFilePath("/docs/reportgen_api.pdf");
            doc4.setDocumentVersion("v2.0");
            doc4.setUploadTimestamp(LocalDateTime.now().minusDays(10));
            doc4.setSoftwareRelease(releaseBeta1);
            doc4.setDocumentType(apiDocType);
            doc4.setUploader(devUser3);
            doc4.setStatus(DocumentStatus.APPROVED);
            documentRepository.save(doc4);

            Document doc5 = new Document();
            doc5.setTitle("ReportGenerator User Guide");
            doc5.setFilePath("/docs/reportgen_guide.pdf");
            doc5.setDocumentVersion("v2.0");
            doc5.setUploadTimestamp(LocalDateTime.now().minusDays(8));
            doc5.setSoftwareRelease(releaseBeta1);
            doc5.setDocumentType(userManualType);
            doc5.setUploader(devUser3);
            doc5.setStatus(DocumentStatus.PENDING_REVIEW);
            documentRepository.save(doc5);

            // Document for product without team (only visible to Admin/Manager)
            Document doc6 = new Document();
            doc6.setTitle("Legacy System Documentation");
            doc6.setFilePath("/docs/legacy_docs.pdf");
            doc6.setDocumentVersion("v0.9");
            doc6.setUploadTimestamp(LocalDateTime.now().minusYears(1));
            doc6.setSoftwareRelease(releaseNoTeam);
            doc6.setDocumentType(specType);
            doc6.setUploader(adminUser);
            doc6.setStatus(DocumentStatus.ARCHIVED);
            documentRepository.save(doc6);

            // ============================================
            // SUMMARY LOG
            // ============================================
            System.out.println("========================================");
            System.out.println("TEST DATA LOADED SUCCESSFULLY!");
            System.out.println("========================================");
            System.out.println("USERS:");
            System.out.println("  - admin@doc.com / admin123 (ADMIN) - Sees ALL documents");
            System.out.println("  - ana.popescu@doc.com / manager123 (PROJECT_MANAGER) - Sees ALL documents");
            System.out.println("  - ion.vasilescu@doc.com / dev123 (TEAM_MEMBER, Echipa Alpha) - Sees 3 docs");
            System.out.println("  - maria.ionescu@doc.com / dev123 (TEAM_MEMBER, NO TEAM) - Sees 0 docs");
            System.out.println("  - andrei.dumitru@doc.com / dev123 (TEAM_MEMBER, Echipa Beta) - Sees 2 docs");
            System.out.println("========================================");
            System.out.println("TEAMS:");
            System.out.println("  - Echipa Alpha -> DocManagement V1 (3 documents)");
            System.out.println("  - Echipa Beta -> ReportGenerator Pro (2 documents)");
            System.out.println("  - No Team -> Legacy System (1 document, Admin/Manager only)");
            System.out.println("========================================");
        };
    }
}