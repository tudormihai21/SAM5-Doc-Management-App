package com.example.docmanagement;

import com.example.docmanagement.Domain.Document.Document;
import com.example.docmanagement.Domain.Document.DocumentStatus;
import com.example.docmanagement.Domain.Document.DocumentType;
import com.example.docmanagement.Domain.Product.SoftwareProduct;
import com.example.docmanagement.Domain.Product.SoftwareRelease;
import com.example.docmanagement.Domain.User.User;
import com.example.docmanagement.Repositories.*;
import com.example.docmanagement.Services.ComputationServices.ReleaseComputationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional

@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/docmanagement",
        "spring.datasource.username=postgres",
        "spring.datasource.password=WSXpl0123",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.jpa.show-sql=true"
})
class ReleaseComputationServiceImplTest {

    @Autowired
    private ReleaseComputationService releaseComputationService;


    @Autowired private UserRepository userRepository;
    @Autowired private SoftwareReleaseRepository releaseRepository;
    @Autowired private SoftwareProductRepository productRepository;
    @Autowired private DocumentTypeRepository documentTypeRepository;
    @Autowired private DocumentRepository documentRepository;

    @Test
    void testCountDocumentsForRelease_RealDatabase() {

        User u = new User();
        u.setFirstName("Test");
        u.setLastName("Counter");
        u.setEmail("counter@test.com");
        u.setPassword("pass");
        u = userRepository.save(u);

        SoftwareProduct prod = new SoftwareProduct();
        prod.setProductName("App de Calcul");
        prod = productRepository.save(prod);

        SoftwareRelease release = new SoftwareRelease();
        release.setVersionNumber("1.5.0");
        release.setReleaseDate(LocalDate.now());
        release.setProduct(prod);
        release = releaseRepository.save(release);


        DocumentType type = new DocumentType();
        type.setTypeName("Raport");
        type = documentTypeRepository.save(type);


        createAndSaveDoc("Doc 1", u, release, type);
        createAndSaveDoc("Doc 2", u, release, type);


        System.out.println("Documente salvate în DB pentru test: " + documentRepository.count());


        long count = releaseComputationService.countDocumentsForRelease(release.getReleaseId());


        assertEquals(2, count, "Serviciul ar trebui să găsească 2 documente în baza de date.");
    }


    private void createAndSaveDoc(String title, User u, SoftwareRelease r, DocumentType t) {
        Document d = new Document();
        d.setTitle(title);
        d.setUploader(u);
        d.setSoftwareRelease(r);
        d.setDocumentType(t);
        d.setUploadTimestamp(LocalDateTime.now());
        d.setStatus(DocumentStatus.DRAFT);
        d.setFilePath("/tmp/" + title);
        documentRepository.save(d);
    }
}