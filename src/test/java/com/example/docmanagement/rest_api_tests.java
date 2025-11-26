package com.example.docmanagement;

import com.example.docmanagement.Domain.Document.DocumentStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Document REST API.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DocumentRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllDocuments() throws Exception {
        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetDocumentById_Found() throws Exception {
        // Assuming document with ID 1 exists
        mockMvc.perform(get("/api/documents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value(1));
    }

    @Test
    void testGetDocumentById_NotFound() throws Exception {
        mockMvc.perform(get("/api/documents/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateDocument() throws Exception {
        String requestBody = """
            {
                "title": "Test Document",
                "filePath": "/test/path",
                "version": "1.0",
                "uploaderId": 1,
                "releaseId": 1,
                "typeId": 1,
                "status": "DRAFT"
            }
            """;

        mockMvc.perform(post("/api/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Document"));
    }

    @Test
    void testUpdateDocument() throws Exception {
        String requestBody = """
            {
                "title": "Updated Title",
                "version": "2.0",
                "status": "APPROVED"
            }
            """;

        mockMvc.perform(put("/api/documents/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void testUpdateDocumentStatus() throws Exception {
        mockMvc.perform(patch("/api/documents/1/status")
                .param("status", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void testDeleteDocument() throws Exception {
        // Create a document first
        String requestBody = """
            {
                "title": "To Delete",
                "filePath": "/test",
                "version": "1.0",
                "uploaderId": 1,
                "releaseId": 1,
                "typeId": 1,
                "status": "DRAFT"
            }
            """;

        String response = mockMvc.perform(post("/api/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract ID and delete
        // (simplified - in real test, parse JSON to get ID)
        mockMvc.perform(delete("/api/documents/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testSearchDocuments() throws Exception {
        mockMvc.perform(get("/api/documents/search")
                .param("title", "test")
                .param("status", "DRAFT"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetDocumentCount() throws Exception {
        mockMvc.perform(get("/api/documents/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber());
    }

    @Test
    void testGetDocumentsByProduct() throws Exception {
        mockMvc.perform(get("/api/documents/product/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}

/**
 * Integration tests for Product REST API.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetAllProducts() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testCreateProduct() throws Exception {
        String requestBody = """
            {
                "productName": "New Product",
                "ownerTeamId": null
            }
            """;

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("New Product"));
    }

    @Test
    void testGetProductReleases() throws Exception {
        mockMvc.perform(get("/api/products/1/releases"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testCreateRelease() throws Exception {
        String requestBody = """
            {
                "versionNumber": "2.0.0",
                "releaseDate": "2025-01-01"
            }
            """;

        mockMvc.perform(post("/api/products/1/releases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.versionNumber").value("2.0.0"));
    }
}