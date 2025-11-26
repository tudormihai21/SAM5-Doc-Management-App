package com.example.docmanagement.Controllers.Rest;

import com.example.docmanagement.Domain.Document.Document;
import com.example.docmanagement.Domain.Document.DocumentStatus;
import com.example.docmanagement.Repositories.DocumentRepository;
import com.example.docmanagement.Services.WorkFlowSer.DocumentWorkflowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Document operations (Sprint 4).
 * Provides RESTful API endpoints for document management.
 */
@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*") // Configure appropriately for production
public class DocumentRestController {

    private final DocumentRepository documentRepository;
    private final DocumentWorkflowService documentWorkflowService;

    public DocumentRestController(DocumentRepository documentRepository,
                                  DocumentWorkflowService documentWorkflowService) {
        this.documentRepository = documentRepository;
        this.documentWorkflowService = documentWorkflowService;
    }

    /**
     * GET /api/documents - Retrieve all documents
     *
     * @return List of all documents
     */
    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {
        List<Document> documents = documentRepository.findAllWithDetails();
        return ResponseEntity.ok(documents);
    }

    /**
     * GET /api/documents/{id} - Retrieve a specific document
     *
     * @param id Document ID
     * @return Document if found, 404 otherwise
     */
    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable int id) {
        return documentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/documents/product/{productId} - Get documents by product
     *
     * @param productId Product ID
     * @return List of documents for the product
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Document>> getDocumentsByProduct(@PathVariable int productId) {
        List<Document> documents = documentRepository.findByProductId(productId);
        return ResponseEntity.ok(documents);
    }

    /**
     * GET /api/documents/status/{status} - Get documents by status
     *
     * @param status Document status
     * @return List of documents with specified status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Document>> getDocumentsByStatus(@PathVariable DocumentStatus status) {
        // Assuming you have this method in repository
        List<Document> documents = documentRepository.findAll().stream()
                .filter(d -> d.getStatus() == status)
                .toList();
        return ResponseEntity.ok(documents);
    }

    /**
     * POST /api/documents - Create a new document
     *
     * @param request Document creation request
     * @return Created document with 201 status
     */
    @PostMapping
    public ResponseEntity<Document> createDocument(@RequestBody DocumentCreateRequest request) {
        try {
            Document created = documentWorkflowService.uploadNewDocument(
                    request.title(),
                    request.filePath(),
                    request.version(),
                    request.uploaderId(),
                    request.releaseId(),
                    request.typeId(),
                    request.status()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT /api/documents/{id} - Update an existing document
     *
     * @param id Document ID
     * @param request Update request
     * @return Updated document or 404
     */
    @PutMapping("/{id}")
    public ResponseEntity<Document> updateDocument(@PathVariable int id,
                                                   @RequestBody DocumentUpdateRequest request) {
        return documentRepository.findById(id)
                .map(document -> {
                    if (request.title() != null) {
                        document.setTitle(request.title());
                    }
                    if (request.status() != null) {
                        document.setStatus(request.status());
                    }
                    if (request.version() != null) {
                        document.setDocumentVersion(request.version());
                    }
                    Document updated = documentRepository.save(document);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PATCH /api/documents/{id}/status - Update document status only
     *
     * @param id Document ID
     * @param status New status
     * @return Updated document or 404
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Document> updateDocumentStatus(@PathVariable int id,
                                                         @RequestParam DocumentStatus status) {
        return documentRepository.findById(id)
                .map(document -> {
                    document.setStatus(status);
                    Document updated = documentRepository.save(document);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/documents/{id} - Delete a document
     *
     * @param id Document ID
     * @return 204 No Content if successful, 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable int id) {
        if (documentRepository.existsById(id)) {
            documentRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * GET /api/documents/search - Search documents
     *
     * @param title Title search term (optional)
     * @param status Status filter (optional)
     * @return List of matching documents
     */
    @GetMapping("/search")
    public ResponseEntity<List<Document>> searchDocuments(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) DocumentStatus status) {

        List<Document> documents = documentRepository.findAll().stream()
                .filter(d -> title == null ||
                        d.getTitle().toLowerCase().contains(title.toLowerCase()))
                .filter(d -> status == null || d.getStatus() == status)
                .toList();

        return ResponseEntity.ok(documents);
    }

    /**
     * GET /api/documents/count - Get total document count
     *
     * @return Total count
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getDocumentCount() {
        long count = documentRepository.count();
        return ResponseEntity.ok(count);
    }

    /**
     * GET /api/documents/release/{releaseId}/count - Count documents for release
     *
     * @param releaseId Release ID
     * @return Document count for the release
     */
    @GetMapping("/release/{releaseId}/count")
    public ResponseEntity<Long> countDocumentsForRelease(@PathVariable int releaseId) {
        long count = documentRepository.countBySoftwareRelease_ReleaseId(releaseId);
        return ResponseEntity.ok(count);
    }
}

/**
 * DTO for creating documents
 */
record DocumentCreateRequest(
        String title,
        String filePath,
        String version,
        int uploaderId,
        int releaseId,
        int typeId,
        DocumentStatus status
) {}

/**
 * DTO for updating documents
 */
record DocumentUpdateRequest(
        String title,
        String version,
        DocumentStatus status
) {}