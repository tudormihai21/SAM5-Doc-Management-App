package com.example.docmanagement;

import com.example.docmanagement.Domain.Document.Document;
import com.example.docmanagement.Domain.Document.DocumentStatus;
import com.example.docmanagement.Domain.Document.DocumentType;
import com.example.docmanagement.Domain.Product.SoftwareProduct;
import com.example.docmanagement.Domain.Product.SoftwareRelease;
import com.example.docmanagement.Domain.User.User;
import com.example.docmanagement.Repositories.*;
import com.example.docmanagement.Services.WorkFlowSer.DocumentWorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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
class DocumentWorkflowServiceImplTest {

    @Autowired
    private DocumentWorkflowService documentWorkflowService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired private UserRepository userRepository;
    @Autowired private SoftwareReleaseRepository releaseRepository;
    @Autowired private SoftwareProductRepository productRepository;
    @Autowired private DocumentTypeRepository documentTypeRepository;

    @Test
    void testUploadNewDocument_SavesToDatabase() {

        User user = new User();
        user.setFirstName("Ion");
        user.setLastName("Popescu");
        user.setEmail("ion@test.com");
        user.setPassword("secret");
        User savedUser = userRepository.save(user);

        SoftwareProduct product = new SoftwareProduct();
        product.setProductName("Soft Bancar");
        SoftwareProduct savedProduct = productRepository.save(product);

        SoftwareRelease release = new SoftwareRelease();
        release.setVersionNumber("2.0");
        release.setReleaseDate(LocalDate.now());
        release.setProduct(savedProduct);
        SoftwareRelease savedRelease = releaseRepository.save(release);

        DocumentType type = new DocumentType();
        type.setTypeName("Specificatii Tehnice");
        DocumentType savedType = documentTypeRepository.save(type);


        String titluDocument = "Plan de Testare V1";

        Document createdDoc = documentWorkflowService.uploadNewDocument(
                titluDocument,
                "/server/docs/plan.pdf",
                "1.0",
                savedUser.getUserId(),
                savedRelease.getReleaseId(),
                savedType.getTypeId(),
                DocumentStatus.DRAFT
        );




        assertNotNull(createdDoc.getDocumentId(), "ID-ul nu trebuie să fie null (trebuie generat de DB)");


        Document fromDb = documentRepository.findById(createdDoc.getDocumentId()).orElse(null);

        assertNotNull(fromDb, "Documentul nu a fost găsit în baza de date!");
        assertEquals(titluDocument, fromDb.getTitle());
        assertEquals(savedUser.getUserId(), fromDb.getUploader().getUserId());

        System.out.println("Testul a confirmat: Documentul cu ID " + fromDb.getDocumentId() + " este în baza de date.");
    }
}