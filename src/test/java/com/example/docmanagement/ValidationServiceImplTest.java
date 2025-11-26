package com.example.docmanagement;

import com.example.docmanagement.Domain.Document.DocumentType;
import com.example.docmanagement.Domain.Product.SoftwareProduct;
import com.example.docmanagement.Domain.Product.SoftwareRelease;
import com.example.docmanagement.Domain.User.User;
import com.example.docmanagement.Repositories.DocumentTypeRepository;
import com.example.docmanagement.Repositories.SoftwareProductRepository;
import com.example.docmanagement.Repositories.SoftwareReleaseRepository;
import com.example.docmanagement.Repositories.UserRepository;
import com.example.docmanagement.Services.ValidationServices.ValidationService;
import jakarta.persistence.EntityNotFoundException;
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
class ValidationServiceImplTest {

    @Autowired
    private ValidationService validationService;

    @Autowired private UserRepository userRepository;
    @Autowired private SoftwareReleaseRepository releaseRepository;
    @Autowired private DocumentTypeRepository documentTypeRepository;
    @Autowired private SoftwareProductRepository productRepository;

    @Test
    void testValidateDocumentUploadPrerequisites_Success() {

        User user = new User();
        user.setFirstName("test_valid_user");
        user.setLastName("test_valid_user");
        user.setEmail("test@example.com");
        user.setPassword("pass123");

        User savedUser = userRepository.save(user);


        SoftwareProduct product = new SoftwareProduct();
        product.setProductName("Test Product");
        SoftwareProduct savedProduct = productRepository.save(product);


        SoftwareRelease release = new SoftwareRelease();
        release.setVersionNumber("1.0.0");
        release.setReleaseDate(LocalDate.now());
        release.setProduct(savedProduct);
        SoftwareRelease savedRelease = releaseRepository.save(release);

        DocumentType type = new DocumentType();
        type.setTypeName("Specifications");
        DocumentType savedType = documentTypeRepository.save(type);

        assertDoesNotThrow(() ->
                validationService.validateDocumentUploadPrerequisites(
                        savedUser.getUserId(),
                        savedRelease.getReleaseId(),
                        savedType.getTypeId()
                )
        );
    }

    @Test
    void testValidateDocumentUploadPrerequisites_UserNotFound() {
        int nonExistentUserId = 99999;

        DocumentType type = new DocumentType();
        type.setTypeName("TestType");
        DocumentType savedType = documentTypeRepository.save(type);

        assertThrows(EntityNotFoundException.class, () ->
                validationService.validateDocumentUploadPrerequisites(
                        nonExistentUserId,
                        1,
                        savedType.getTypeId()
                )
        );
    }
}