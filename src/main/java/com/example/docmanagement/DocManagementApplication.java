package com.example.docmanagement;

import com.example.docmanagement.Repositories.*;
// Importă toate Entitățile de care ai nevoie
import com.example.docmanagement.Domain.Document.*;
import com.example.docmanagement.Domain.Product.*;
import com.example.docmanagement.Domain.Team.*;
import com.example.docmanagement.Domain.User.*;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder; // Importă encoder-ul

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


            Role adminRole = new Role();
            adminRole.setRoleName("ADMIN");
            adminRole = roleRepository.save(adminRole);

            Role managerRole = new Role();
            managerRole.setRoleName("PROJECT_MANAGER");
            managerRole = roleRepository.save(managerRole);

            Role memberRole = new Role();
            memberRole.setRoleName("TEAM_MEMBER");
            memberRole = roleRepository.save(memberRole);


            User adminUser = new User();
            adminUser.setFirstName("Admin");
            adminUser.setLastName("Site");
            adminUser.setEmail("admin@doc.com");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setRole(adminRole); // Acum 'adminRole' are un ID
            userRepository.save(adminUser);

            User managerUser = new User();
            managerUser.setFirstName("Ana");
            managerUser.setLastName("Popescu");
            managerUser.setEmail("ana.popescu@doc.com");
            managerUser.setPassword(passwordEncoder.encode("manager123"));
            managerUser.setRole(managerRole);
            userRepository.save(managerUser);

            User devUser = new User();
            devUser.setFirstName("Ion");
            devUser.setLastName("Vasilescu");
            devUser.setEmail("ion.vasilescu@doc.com");
            devUser.setPassword(passwordEncoder.encode("dev123"));
            devUser.setRole(memberRole);
            userRepository.save(devUser);

            Team teamAlpha = new Team();
            teamAlpha.setTeamName("Echipa Alpha");
            teamAlpha = teamRepository.save(teamAlpha);

            SoftwareProduct product = new SoftwareProduct();
            product.setProductName("DocManagement V1");
            product.setOwnerTeam(teamAlpha);
            product = productRepository.save(product);

            SoftwareRelease release = new SoftwareRelease();
            release.setVersionNumber("1.0.0-BETA");
            release.setReleaseDate(LocalDate.now());
            release.setProduct(product);
            release = releaseRepository.save(release);

            DocumentType specType = new DocumentType();
            specType.setTypeName("Specificatii Tehnice");
            specType = typeRepository.save(specType);

            Document testDoc = new Document();
            testDoc.setTitle("Specificatii API Login");
            testDoc.setFilePath("/docs/api_login_v1.pdf");
            testDoc.setDocumentVersion("v1.0");
            testDoc.setUploadTimestamp(LocalDateTime.now());
            testDoc.setSoftwareRelease(release);
            testDoc.setDocumentType(specType);
            testDoc.setUploader(devUser);
            testDoc.setStatus(DocumentStatus.PENDING_REVIEW);

            documentRepository.save(testDoc);
        };
    }

}