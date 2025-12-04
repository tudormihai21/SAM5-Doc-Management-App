package com.example.docmanagement.Controllers.Rest;

import com.example.docmanagement.Domain.Document.Document;
import com.example.docmanagement.Domain.Document.DocumentStatus;
import com.example.docmanagement.Repositories.DocumentRepository;
import com.example.docmanagement.Repositories.DocumentTypeRepository;
import com.example.docmanagement.Repositories.SoftwareReleaseRepository;
import com.example.docmanagement.Repositories.UserRepository;
import com.example.docmanagement.Services.FileStorage.FileStorageService;
import com.example.docmanagement.Services.FileStorage.StorageFileNotFoundException;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Sprint 5: REST Controller for File Upload/Download Operations
 *
 * Provides RESTful endpoints for:
 * - Uploading documents with metadata
 * - Downloading stored documents
 * - Listing uploaded files
 * - Deleting files
 *
 * This controller works alongside DocumentRestController for complete
 * document management functionality.
 */
@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileUploadRestController {

    private final FileStorageService fileStorageService;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final SoftwareReleaseRepository releaseRepository;
    private final DocumentTypeRepository documentTypeRepository;

    public FileUploadRestController(
            FileStorageService fileStorageService,
            DocumentRepository documentRepository,
            UserRepository userRepository,
            SoftwareReleaseRepository releaseRepository,
            DocumentTypeRepository documentTypeRepository) {
        this.fileStorageService = fileStorageService;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.releaseRepository = releaseRepository;
        this.documentTypeRepository = documentTypeRepository;
    }

    /**
     * POST /api/files/upload - Upload a single file with document metadata
     *
     * Creates a new Document entity and stores the physical file.
     *
     * @param file The uploaded file
     * @param title Document title
     * @param version Document version
     * @param uploaderId User ID of uploader
     * @param releaseId Software Release ID
     * @param typeId Document Type ID
     * @param status Document status (optional, defaults to DRAFT)
     * @return Created document with file information
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("version") String version,
            @RequestParam("uploaderId") int uploaderId,
            @RequestParam("releaseId") int releaseId,
            @RequestParam("typeId") int typeId,
            @RequestParam(value = "status", defaultValue = "DRAFT") DocumentStatus status) {

        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Please select a file to upload"));
            }

            // Validate file type (optional - add more types as needed)
            String contentType = file.getContentType();
            if (!isAllowedFileType(contentType)) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("File type not allowed: " + contentType));
            }

            // Validate referenced entities exist
            var uploader = userRepository.findById(uploaderId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + uploaderId));
            var release = releaseRepository.findById(releaseId)
                    .orElseThrow(() -> new IllegalArgumentException("Release not found: " + releaseId));
            var docType = documentTypeRepository.findById(typeId)
                    .orElseThrow(() -> new IllegalArgumentException("Document type not found: " + typeId));

            // Create document entity first to get ID
            Document document = new Document();
            document.setTitle(title);
            document.setDocumentVersion(version);
            document.setUploadTimestamp(LocalDateTime.now());
            document.setUploader(uploader);
            document.setSoftwareRelease(release);
            document.setDocumentType(docType);
            document.setStatus(status);
            document.setFilePath("pending"); // Temporary, will update after storing

            Document savedDoc = documentRepository.save(document);

            // Store the file
            String storedFilename = fileStorageService.store(file, savedDoc.getDocumentId());

            // Update document with actual file path
            savedDoc.setFilePath(storedFilename);
            savedDoc = documentRepository.save(savedDoc);

            // Build download URL
            String downloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/files/download/")
                    .path(storedFilename)
                    .toUriString();

            // Return response with file info
            FileUploadResponse response = new FileUploadResponse(
                    savedDoc.getDocumentId(),
                    savedDoc.getTitle(),
                    file.getOriginalFilename(),
                    storedFilename,
                    downloadUrl,
                    file.getSize(),
                    file.getContentType(),
                    "File uploaded successfully"
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to upload file: " + e.getMessage()));
        }
    }

    /**
     * POST /api/files/upload-multiple - Upload multiple files
     *
     * @param files Array of files to upload
     * @param releaseId Associated release ID
     * @param typeId Document type ID
     * @param uploaderId Uploader user ID
     * @return List of upload results
     */
    @PostMapping("/upload-multiple")
    public ResponseEntity<?> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("releaseId") int releaseId,
            @RequestParam("typeId") int typeId,
            @RequestParam("uploaderId") int uploaderId) {

        try {
            List<FileUploadResponse> responses = new ArrayList<>();

            var uploader = userRepository.findById(uploaderId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + uploaderId));
            var release = releaseRepository.findById(releaseId)
                    .orElseThrow(() -> new IllegalArgumentException("Release not found: " + releaseId));
            var docType = documentTypeRepository.findById(typeId)
                    .orElseThrow(() -> new IllegalArgumentException("Document type not found: " + typeId));

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    // Create document
                    Document document = new Document();
                    document.setTitle(file.getOriginalFilename());
                    document.setDocumentVersion("1.0");
                    document.setUploadTimestamp(LocalDateTime.now());
                    document.setUploader(uploader);
                    document.setSoftwareRelease(release);
                    document.setDocumentType(docType);
                    document.setStatus(DocumentStatus.DRAFT);
                    document.setFilePath("pending");

                    Document savedDoc = documentRepository.save(document);

                    // Store file
                    String storedFilename = fileStorageService.store(file, savedDoc.getDocumentId());
                    savedDoc.setFilePath(storedFilename);
                    documentRepository.save(savedDoc);

                    String downloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/api/files/download/")
                            .path(storedFilename)
                            .toUriString();

                    responses.add(new FileUploadResponse(
                            savedDoc.getDocumentId(),
                            savedDoc.getTitle(),
                            file.getOriginalFilename(),
                            storedFilename,
                            downloadUrl,
                            file.getSize(),
                            file.getContentType(),
                            "Uploaded successfully"
                    ));
                }
            }

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to upload files: " + e.getMessage()));
        }
    }


    @GetMapping("/download/**")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam(required = false) String path,
            jakarta.servlet.http.HttpServletRequest request) {

        try {
            // Extract the file path from the URL
            String requestURL = request.getRequestURL().toString();
            String filePath = requestURL.substring(requestURL.indexOf("/download/") + "/download/".length());

            Resource resource = fileStorageService.loadAsResource(filePath);

            // Determine content type
            String contentType = "application/octet-stream";
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (Exception e) {
                // Use default content type
            }

            // Extract filename for Content-Disposition header
            String filename = filePath.contains("/")
                    ? filePath.substring(filePath.lastIndexOf("/") + 1)
                    : filePath;

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (StorageFileNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/files/document/{documentId} - Download file by document ID
     */
    @GetMapping("/document/{documentId}")
    public ResponseEntity<Resource> downloadByDocumentId(
            @PathVariable int documentId,
            jakarta.servlet.http.HttpServletRequest request) {

        // Find document first
        var documentOpt = documentRepository.findById(documentId);

        if (documentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Document doc = documentOpt.get();

        try {
            Resource resource = fileStorageService.loadAsResource(doc.getFilePath());

            String contentType = "application/octet-stream";
            try {
                contentType = request.getServletContext()
                        .getMimeType(resource.getFile().getAbsolutePath());
            } catch (Exception e) {
                // Use default content type
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + doc.getTitle() + "\"")
                    .body(resource);
        } catch (StorageFileNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/files/{documentId} - Delete a file and its document record
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<?> deleteFile(@PathVariable int documentId) {
        return documentRepository.findById(documentId)
                .map(doc -> {
                    try {
                        // Delete physical file
                        if (doc.getFilePath() != null && !doc.getFilePath().equals("pending")) {
                            fileStorageService.delete(doc.getFilePath());
                        }

                        // Delete document record
                        documentRepository.delete(doc);

                        return ResponseEntity.ok(Map.of(
                                "message", "File and document deleted successfully",
                                "documentId", documentId
                        ));
                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new ErrorResponse("Failed to delete: " + e.getMessage()));
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/files/list - List all stored files
     */
    @GetMapping("/list")
    public ResponseEntity<?> listFiles() {
        try {
            List<String> files = fileStorageService.loadAll()
                    .map(path -> path.toString())
                    .toList();
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to list files: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/files/{documentId}/replace - Replace existing file
     */
    @PutMapping("/{documentId}/replace")
    public ResponseEntity<?> replaceFile(
            @PathVariable int documentId,
            @RequestParam("file") MultipartFile file) {

        return documentRepository.findById(documentId)
                .map(doc -> {
                    try {
                        // Delete old file
                        if (doc.getFilePath() != null && !doc.getFilePath().equals("pending")) {
                            fileStorageService.delete(doc.getFilePath());
                        }

                        // Store new file
                        String newPath = fileStorageService.store(file, documentId);
                        doc.setFilePath(newPath);
                        doc.setUploadTimestamp(LocalDateTime.now());
                        documentRepository.save(doc);

                        String downloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/api/files/download/")
                                .path(newPath)
                                .toUriString();

                        return ResponseEntity.ok(new FileUploadResponse(
                                doc.getDocumentId(),
                                doc.getTitle(),
                                file.getOriginalFilename(),
                                newPath,
                                downloadUrl,
                                file.getSize(),
                                file.getContentType(),
                                "File replaced successfully"
                        ));
                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new ErrorResponse("Failed to replace file: " + e.getMessage()));
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Check if file type is allowed
     */
    private boolean isAllowedFileType(String contentType) {
        if (contentType == null) return false;

        // Allow common document types
        return contentType.equals("application/pdf") ||
                contentType.equals("application/msword") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                contentType.equals("application/vnd.ms-excel") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                contentType.equals("text/plain") ||
                contentType.equals("text/csv") ||
                contentType.equals("application/json") ||
                contentType.equals("application/xml") ||
                contentType.startsWith("image/") ||
                contentType.equals("application/zip") ||
                contentType.equals("application/x-zip-compressed");
    }
}

/**
 * DTO for file upload response
 */
record FileUploadResponse(
        int documentId,
        String title,
        String originalFilename,
        String storedFilename,
        String downloadUrl,
        long fileSize,
        String contentType,
        String message
) {}