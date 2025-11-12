package com.example.docmanagement.Services.WorkFlowSer;

import com.example.docmanagement.Domain.Document.DocumentStatus;
import com.example.docmanagement.Services.ValidationServices.ValidationService;
import com.example.docmanagement.Domain.Document.Document;
import com.example.docmanagement.Repositories.DocumentRepository;
import com.example.docmanagement.Repositories.DocumentTypeRepository;
import com.example.docmanagement.Repositories.SoftwareReleaseRepository;
import com.example.docmanagement.Repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DocumentWorkflowServiceImpl implements DocumentWorkflowService {

    private final ValidationService validationService;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final SoftwareReleaseRepository releaseRepository;
    private final DocumentTypeRepository documentTypeRepository;

    // NOTĂ: Aici ar intra și un AuditService sau ComputationService dacă ar fi nevoie.

    public DocumentWorkflowServiceImpl(ValidationService validationService,
                                       DocumentRepository documentRepository,
                                       UserRepository userRepository,
                                       SoftwareReleaseRepository releaseRepository,
                                       DocumentTypeRepository documentTypeRepository) {
        this.validationService = validationService;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.releaseRepository = releaseRepository;
        this.documentTypeRepository = documentTypeRepository;
    }

    @Override
    @Transactional
    public Document uploadNewDocument(String title, String filePath, String version,
                                      int uploaderId, int releaseId, int typeId , DocumentStatus status) {


        validationService.validateDocumentUploadPrerequisites(uploaderId, releaseId, typeId);


        var uploader = userRepository.getReferenceById(uploaderId);
        var release = releaseRepository.getReferenceById(releaseId);
        var docType = documentTypeRepository.getReferenceById(typeId);


        Document newDocument = new Document();
        newDocument.setTitle(title);
        newDocument.setFilePath(filePath);
        newDocument.setDocumentVersion(version);
        newDocument.setUploadTimestamp(LocalDateTime.now());
        newDocument.setUploader(uploader);
        newDocument.setSoftwareRelease(release);
        newDocument.setDocumentType(docType);


        Document savedDocument = documentRepository.save(newDocument);


        return savedDocument;
    }
}
