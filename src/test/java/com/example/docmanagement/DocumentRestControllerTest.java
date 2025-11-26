package com.example.docmanagement;

import com.example.docmanagement.Controllers.Rest.DocumentCreateRequest;
import com.example.docmanagement.Controllers.Rest.DocumentUpdateRequest;
import com.example.docmanagement.Domain.Document.Document;
import com.example.docmanagement.Domain.Document.DocumentStatus;
import com.example.docmanagement.Repositories.DocumentRepository;
import com.example.docmanagement.Repositories.DocumentTypeRepository;
import com.example.docmanagement.Repositories.SoftwareReleaseRepository;
import com.example.docmanagement.Repositories.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class DocumentRestControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SoftwareReleaseRepository releaseRepository;

    @Autowired
    private DocumentTypeRepository typeRepository;

    private String baseUrl;
    private static Integer createdDocumentId;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/documents";
    }

    @Test
    @Order(1)
    @DisplayName("Test GET /api/documents - Get All Documents")
    void testGetAllDocuments() {

        ResponseEntity<List<Document>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Document>>() {}
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() >= 0);

        System.out.println("GET All Documents: " + response.getBody().size() + " documents found");
    }

    @Test
    @Order(2)
    @DisplayName("Test POST /api/documents - Create Document")
    void testCreateDocument() {

        DocumentCreateRequest request = new DocumentCreateRequest(
                "REST API Test Document",
                "/docs/rest_test.pdf",
                "v1.0",
                userRepository.findAll().get(0).getUserId(),
                releaseRepository.findAll().get(0).getReleaseId(),
                typeRepository.findAll().get(0).getTypeId(),
                DocumentStatus.DRAFT
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DocumentCreateRequest> entity = new HttpEntity<>(request, headers);


        ResponseEntity<Document> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                entity,
                Document.class
        );


        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getDocumentId());
        assertEquals("REST API Test Document", response.getBody().getTitle());
        assertEquals(DocumentStatus.DRAFT, response.getBody().getStatus());


        createdDocumentId = response.getBody().getDocumentId();

        System.out.println("POST Create Document: ID = " + createdDocumentId);
    }


    @Test
    @Order(3)
    @DisplayName("Test GET /api/documents/{id} - Get Document By ID")
    void testGetDocumentById() {

        assertNotNull(createdDocumentId, "Document must be created first");


        ResponseEntity<Document> response = restTemplate.getForEntity(
                baseUrl + "/" + createdDocumentId,
                Document.class
        );


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(createdDocumentId, response.getBody().getDocumentId());
        assertEquals("REST API Test Document", response.getBody().getTitle());

        System.out.println("GET Document By ID: " + response.getBody().getTitle());
    }


    @Test
    @Order(4)
    @DisplayName("Test PUT /api/documents/{id} - Update Document")
    void testUpdateDocument() {

        assertNotNull(createdDocumentId, "Document must be created first");

        DocumentUpdateRequest updateRequest = new DocumentUpdateRequest(
                "Updated REST API Test Document",
                "v2.0",
                DocumentStatus.PENDING_REVIEW
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DocumentUpdateRequest> entity = new HttpEntity<>(updateRequest, headers);


        ResponseEntity<Document> response = restTemplate.exchange(
                baseUrl + "/" + createdDocumentId,
                HttpMethod.PUT,
                entity,
                Document.class
        );


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated REST API Test Document", response.getBody().getTitle());
        assertEquals("v2.0", response.getBody().getDocumentVersion());
        assertEquals(DocumentStatus.PENDING_REVIEW, response.getBody().getStatus());

        System.out.println("PUT Update Document: " + response.getBody().getTitle());
    }

    @Test
    @Order(5)
    @DisplayName("Test PATCH /api/documents/{id}/status - Update Status")
    void testUpdateDocumentStatus() {

        assertNotNull(createdDocumentId, "Document must be created first");


        ResponseEntity<Document> response = restTemplate.exchange(
                baseUrl + "/" + createdDocumentId + "/status?status=APPROVED",
                HttpMethod.PATCH,
                null,
                Document.class
        );


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(DocumentStatus.APPROVED, response.getBody().getStatus());

        System.out.println("PATCH Update Status: " + response.getBody().getStatus());
    }

    @Test
    @Order(6)
    @DisplayName("Test GET /api/documents/status/{status} - Get By Status")
    void testGetDocumentsByStatus() {

        ResponseEntity<List<Document>> response = restTemplate.exchange(
                baseUrl + "/status/APPROVED",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Document>>() {}
        );


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().stream().allMatch(d -> d.getStatus() == DocumentStatus.APPROVED));

        System.out.println("GET By Status: " + response.getBody().size() + " APPROVED documents");
    }

    @Test
    @Order(7)
    @DisplayName("Test GET /api/documents/search - Search Documents")
    void testSearchDocuments() {

        ResponseEntity<List<Document>> response = restTemplate.exchange(
                baseUrl + "/search?title=REST",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Document>>() {}
        );


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() > 0);
        assertTrue(response.getBody().stream()
                .allMatch(d -> d.getTitle().toLowerCase().contains("rest")));

        System.out.println("GET Search: Found " + response.getBody().size() + " documents");
    }

    @Test
    @Order(8)
    @DisplayName("Test GET /api/documents/count - Get Count")
    void testGetDocumentCount() {

        ResponseEntity<Long> response = restTemplate.getForEntity(
                baseUrl + "/count",
                Long.class
        );


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() > 0);

        System.out.println("GET Count: " + response.getBody() + " total documents");
    }

    @Test
    @Order(9)
    @DisplayName("Test DELETE /api/documents/{id} - Delete Document")
    void testDeleteDocument() {

        assertNotNull(createdDocumentId, "Document must be created first");


        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + createdDocumentId,
                HttpMethod.DELETE,
                null,
                Void.class
        );


        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());


        ResponseEntity<Document> getResponse = restTemplate.getForEntity(
                baseUrl + "/" + createdDocumentId,
                Document.class
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());

        System.out.println("DELETE Document: ID = " + createdDocumentId);
    }

    @Test
    @Order(10)
    @DisplayName("Test GET /api/documents/{id} - 404 Not Found")
    void testGetNonExistentDocument() {

        ResponseEntity<Document> response = restTemplate.getForEntity(
                baseUrl + "/99999",
                Document.class
        );


        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        System.out.println("GET Non-Existent: Properly returns 404");
    }

    @Test
    @Order(11)
    @DisplayName("Test GET /api/documents/product/{productId} - Get By Product")
    void testGetDocumentsByProduct() {

        int productId = releaseRepository.findAll().get(0).getProduct().getProductId();


        ResponseEntity<List<Document>> response = restTemplate.exchange(
                baseUrl + "/product/" + productId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Document>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        System.out.println("GET By Product: " + response.getBody().size() + " documents for product " + productId);
    }

    @Test
    @Order(12)
    @DisplayName("Test GET /api/documents/release/{releaseId}/count - Count By Release")
    void testCountDocumentsForRelease() {

        int releaseId = releaseRepository.findAll().get(0).getReleaseId();

        ResponseEntity<Long> response = restTemplate.getForEntity(
                baseUrl + "/release/" + releaseId + "/count",
                Long.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() >= 0);

        System.out.println("GET Count By Release: " + response.getBody() + " documents for release " + releaseId);
    }
}
