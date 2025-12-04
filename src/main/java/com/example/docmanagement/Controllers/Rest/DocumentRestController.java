package com.example.docmanagement.Controllers.Rest;

import com.example.docmanagement.Domain.Document.Document;
import com.example.docmanagement.Domain.Document.DocumentStatus;
import com.example.docmanagement.Repositories.DocumentRepository;
import com.example.docmanagement.Services.WorkFlowSer.DocumentWorkflowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
public class DocumentRestController {

    private final DocumentRepository documentRepository;
    private final DocumentWorkflowService documentWorkflowService;


    //Constructor injection (recommended over @Autowired field injection)

    public DocumentRestController(DocumentRepository documentRepository,
                                  DocumentWorkflowService documentWorkflowService) {
        this.documentRepository = documentRepository;
        this.documentWorkflowService = documentWorkflowService;
    }


    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {
        try {
            List<Document> documents = documentRepository.findAllWithDetails();
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/documents/{id} - Retrieve a specific document
     *
     * @param id Document ID
     * @return Document if found with HTTP 200 OK, 404 NOT FOUND otherwise
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
     * @return List of documents for the product with HTTP 200 OK
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Document>> getDocumentsByProduct(@PathVariable int productId) {
        try {
            List<Document> documents = documentRepository.findByProductId(productId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/documents/status/{status} - Get documents by status
     *
     * @param status Document status
     * @return List of documents with specified status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Document>> getDocumentsByStatus(@PathVariable DocumentStatus status) {
        try {
            List<Document> documents = documentRepository.findAll().stream()
                    .filter(d -> d.getStatus() == status)
                    .toList();
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/documents - Create a new document
     * Uses Business Workflow Service (Sprint 4 requirement)
     *
     * @param request Document creation request (DTO)
     * @return Created document with HTTP 201 CREATED, 400 BAD REQUEST on error
     */
    @PostMapping
    public ResponseEntity<?> createDocument(@RequestBody DocumentCreateRequest request) {
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
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * PUT /api/documents/{id} - Update an existing document
     *
     * @param id Document ID
     * @param request Update request (DTO)
     * @return Updated document with HTTP 200 OK, 404 NOT FOUND if not exists
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDocument(@PathVariable int id,
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
     * @return Updated document with HTTP 200 OK, 404 NOT FOUND if not exists
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
     * @return HTTP 204 NO CONTENT if successful, 404 NOT FOUND if not exists
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
     * @return List of matching documents with HTTP 200 OK
     */
    @GetMapping("/search")
    public ResponseEntity<List<Document>> searchDocuments(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) DocumentStatus status) {
        try {
            List<Document> documents = documentRepository.findAll().stream()
                    .filter(d -> title == null ||
                            d.getTitle().toLowerCase().contains(title.toLowerCase()))
                    .filter(d -> status == null || d.getStatus() == status)
                    .toList();
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/documents/count - Get total document count
     *
     * @return Total count with HTTP 200 OK
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
     * @return Document count for the release with HTTP 200 OK
     */
    @GetMapping("/release/{releaseId}/count")
    public ResponseEntity<Long> countDocumentsForRelease(@PathVariable int releaseId) {
        long count = documentRepository.countBySoftwareRelease_ReleaseId(releaseId);
        return ResponseEntity.ok(count);
    }
}

/**
 DTO for error responses
 **/
record ErrorResponse(String message) {}
