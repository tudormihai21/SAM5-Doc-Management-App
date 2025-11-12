package com.example.docmanagement;

// Importă toate Repozitoriile de care ai nevoie
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
        app.setWebApplicationType(WebApplicationType.SERVLET); // <-- 3. SPUNE-I SĂ FIE UN SERVER WEB
        app.run(args);
    }
    // În DocManagementApplication.java


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
            if (userRepository.count() > 0) { // Verificăm userii, e corect
                return;
            }

            // 1. Creăm Roluri (Varianta CORECTATĂ)
            // Trebuie să re-asignăm variabila pentru a prinde ID-ul generat de BD
            Role adminRole = new Role();
            adminRole.setRoleName("ADMIN");
            adminRole = roleRepository.save(adminRole); // <-- AICI E CORECȚIA

            Role managerRole = new Role();
            managerRole.setRoleName("PROJECT_MANAGER");
            managerRole = roleRepository.save(managerRole); // <-- AICI E CORECȚIA

            Role memberRole = new Role();
            memberRole.setRoleName("TEAM_MEMBER");
            memberRole = roleRepository.save(memberRole); // <-- AICI E CORECȚIA

            // 2. Creăm Utilizatori
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

            // 3. Creăm Echipă și Produs (Varianta CORECTATĂ)
            Team teamAlpha = new Team();
            teamAlpha.setTeamName("Echipa Alpha");
            teamAlpha = teamRepository.save(teamAlpha); // <-- Re-asignare

            SoftwareProduct product = new SoftwareProduct();
            product.setProductName("DocManagement V1");
            product.setOwnerTeam(teamAlpha);
            product = productRepository.save(product); // <-- Re-asignare

            // 4. Creăm un Release (Varianta CORECTATĂ)
            SoftwareRelease release = new SoftwareRelease();
            release.setVersionNumber("1.0.0-BETA");
            release.setReleaseDate(LocalDate.now());
            release.setProduct(product);
            release = releaseRepository.save(release); // <-- Re-asignare

            // 5. Creăm un Tip de Document (Varianta CORECTATĂ)
            DocumentType specType = new DocumentType();
            specType.setTypeName("Specificatii Tehnice");
            specType = typeRepository.save(specType); // <-- Re-asignare

            // 6. Creăm un Document de test
            Document testDoc = new Document();
            testDoc.setTitle("Specificații API Login");
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