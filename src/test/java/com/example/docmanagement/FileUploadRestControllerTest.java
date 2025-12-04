package com.example.docmanagement;

import com.example.docmanagement.Domain.Document.Document;
import com.example.docmanagement.Repositories.DocumentRepository;
import com.example.docmanagement.Repositories.DocumentTypeRepository;
import com.example.docmanagement.Repositories.SoftwareReleaseRepository;
import com.example.docmanagement.Repositories.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sprint 5: File Upload REST Controller Integration Tests
 * 
 * Tests the file upload, download, and management endpoints.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class FileUploadRestControllerTest {

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
    private static Integer uploadedDocumentId;
    private static String uploadedFilePath;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/files";
    }

    @Test
    @Order(1)
    @DisplayName("Test POST /api/files/upload - Upload Single File")
    void testUploadFile() {
        // Skip if no test data available
        if (userRepository.count() == 0 || releaseRepository.count() == 0 || typeRepository.count() == 0) {
            System.out.println("Skipping test - no test data available");
            return;
        }

        // Prepare multipart request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        
        // Create a test file resource
        byte[] fileContent = "This is test content for the document upload test.".getBytes(StandardCharsets.UTF_8);
        ByteArrayResource fileResource = new ByteArrayResource(fileContent) {
            @Override
            public String getFilename() {
                return "test-document.txt";
            }
        };
        
        body.add("file", fileResource);
        body.add("title", "Test Upload Document Sprint 5");
        body.add("version", "1.0");
        body.add("uploaderId", userRepository.findAll().get(0).getUserId());
        body.add("releaseId", releaseRepository.findAll().get(0).getReleaseId());
        body.add("typeId", typeRepository.findAll().get(0).getTypeId());
        body.add("status", "DRAFT");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/upload",
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().get("documentId"));
        assertNotNull(response.getBody().get("storedFilename"));
        assertEquals("File uploaded successfully", response.getBody().get("message"));

        uploadedDocumentId = (Integer) response.getBody().get("documentId");
        uploadedFilePath = (String) response.getBody().get("storedFilename");

        System.out.println("Upload Test: Document ID = " + uploadedDocumentId);
        System.out.println("Upload Test: Stored Path = " + uploadedFilePath);
    }

    @Test
    @Order(2)
    @DisplayName("Test GET /api/files/document/{documentId} - Download by Document ID")
    void testDownloadByDocumentId() {
        if (uploadedDocumentId == null) {
            System.out.println("Skipping - no document uploaded");
            return;
        }

        ResponseEntity<byte[]> response = restTemplate.exchange(
                baseUrl + "/document/" + uploadedDocumentId,
                HttpMethod.GET,
                null,
                byte[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);

        System.out.println("Download Test: File size = " + response.getBody().length + " bytes");
    }

    @Test
    @Order(3)
    @DisplayName("Test GET /api/files/list - List All Files")
    void testListFiles() {
        ResponseEntity<List> response = restTemplate.exchange(
                baseUrl + "/list",
                HttpMethod.GET,
                null,
                List.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        System.out.println("List Test: Found " + response.getBody().size() + " files");
    }

    @Test
    @Order(4)
    @DisplayName("Test PUT /api/files/{documentId}/replace - Replace File")
    void testReplaceFile() {
        if (uploadedDocumentId == null) {
            System.out.println("Skipping - no document uploaded");
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        
        byte[] newContent = "This is the REPLACED content for the document.".getBytes(StandardCharsets.UTF_8);
        ByteArrayResource fileResource = new ByteArrayResource(newContent) {
            @Override
            public String getFilename() {
                return "replaced-document.txt";
            }
        };
        
        body.add("file", fileResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/" + uploadedDocumentId + "/replace",
                HttpMethod.PUT,
                requestEntity,
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("File replaced successfully", response.getBody().get("message"));

        System.out.println("Replace Test: New file path = " + response.getBody().get("storedFilename"));
    }

    @Test
    @Order(5)
    @DisplayName("Test POST /api/files/upload - Upload with Empty File")
    void testUploadEmptyFile() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        
        // Empty file
        ByteArrayResource emptyFile = new ByteArrayResource(new byte[0]) {
            @Override
            public String getFilename() {
                return "empty.txt";
            }
        };
        
        body.add("file", emptyFile);
        body.add("title", "Empty File Test");
        body.add("version", "1.0");
        body.add("uploaderId", 1);
        body.add("releaseId", 1);
        body.add("typeId", 1);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/upload",
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        System.out.println("Empty File Test: Correctly rejected empty file");
    }

    @Test
    @Order(6)
    @DisplayName("Test GET /api/files/document/{id} - Download Non-existent Document")
    void testDownloadNonExistentDocument() {
        ResponseEntity<byte[]> response = restTemplate.exchange(
                baseUrl + "/document/99999",
                HttpMethod.GET,
                null,
                byte[].class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        System.out.println("Non-existent Document Test: Correctly returned 404");
    }

    @Test
    @Order(7)
    @DisplayName("Test DELETE /api/files/{documentId} - Delete Document and File")
    void testDeleteFile() {
        if (uploadedDocumentId == null) {
            System.out.println("Skipping - no document to delete");
            return;
        }

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/" + uploadedDocumentId,
                HttpMethod.DELETE,
                null,
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("File and document deleted successfully", response.getBody().get("message"));

        // Verify deletion
        assertFalse(documentRepository.existsById(uploadedDocumentId));

        System.out.println("Delete Test: Document " + uploadedDocumentId + " deleted successfully");
    }

    @Test
    @Order(8)
    @DisplayName("Test POST /api/files/upload-multiple - Upload Multiple Files")
    void testUploadMultipleFiles() {
        if (userRepository.count() == 0 || releaseRepository.count() == 0 || typeRepository.count() == 0) {
            System.out.println("Skipping test - no test data available");
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        
        // Create multiple test files
        for (int i = 1; i <= 3; i++) {
            final int fileNum = i;
            byte[] content = ("Content of file " + i).getBytes(StandardCharsets.UTF_8);
            ByteArrayResource fileResource = new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return "batch-file-" + fileNum + ".txt";
                }
            };
            body.add("files", fileResource);
        }
        
        body.add("uploaderId", userRepository.findAll().get(0).getUserId());
        body.add("releaseId", releaseRepository.findAll().get(0).getReleaseId());
        body.add("typeId", typeRepository.findAll().get(0).getTypeId());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<List> response = restTemplate.exchange(
                baseUrl + "/upload-multiple",
                HttpMethod.POST,
                requestEntity,
                List.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());

        System.out.println("Multiple Upload Test: Uploaded " + response.getBody().size() + " files");
    }
}
